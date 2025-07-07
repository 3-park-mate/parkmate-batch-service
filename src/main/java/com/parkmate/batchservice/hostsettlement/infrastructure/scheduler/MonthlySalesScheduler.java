package com.parkmate.batchservice.hostsettlement.infrastructure.scheduler;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.kafka.buffer.ReservationChunkBuffer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySalesScheduler {

    private final JobLauncher jobLauncher;
    private final Job monthlySalesJob;
    private final HostSettlementService hostSettlementService;

    // 테스트용 1분마다 실행
    @Scheduled(cron = "0 */1 * * * *")
    public void triggerAllMonthlyJobs() {
        log.info("🚀 [정산 스케줄러] 월 정산 스케줄 시작");
        runMidMonthJob();
        runEndOfMonthJob();
        log.info("✅ [정산 스케줄러] 월 정산 스케줄 종료");
    }

    public void runMidMonthJob() {
        log.info("🚀 [정산 스케줄러] [15일 정산] 시작");
        executeMonthlySettlement(SettlementCycle.FIFTEEN);
    }

    public void runEndOfMonthJob() {
        log.info("🚀 [정산 스케줄러] [말일 정산] 시작");
        executeMonthlySettlement(SettlementCycle.THIRTY);
    }

    private void executeMonthlySettlement(SettlementCycle cycle) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = getStartDate(currentMonth, cycle);
        LocalDate endDate = getEndDate(currentMonth, cycle);

        // ✅ 변경된 부분: 정산 테이블에서 정산 대상 추출
        List<HostParkingLotDto> targets = hostSettlementService.extractMonthlyTargetsFromDB(currentMonth, cycle);

        if (targets.isEmpty()) {
            log.info("📭 [정산 스케줄러] 정산 대상 없음 - cycle={}", cycle);
            return;
        }

        for (HostParkingLotDto target : targets) {
            launchJob(target, currentMonth, cycle, startDate, endDate);
        }

        log.info("🏁 [정산 스케줄러] {} 정산 처리 완료 - 기간: {} ~ {}", cycle, startDate, endDate);
    }

    private void launchJob(HostParkingLotDto target, YearMonth yearMonth, SettlementCycle cycle,
                           LocalDate start, LocalDate end) {
        JobParameters parameters = new JobParametersBuilder()
                .addString("hostUuid", target.getHostUuid())
                .addString("parkingLotUuid", target.getParkingLotUuid())
                .addLong("year", (long) yearMonth.getYear())
                .addLong("month", (long) yearMonth.getMonthValue())
                .addString("settlementCycle", cycle.name())
                .addString("start", start.toString())
                .addString("end", end.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(monthlySalesJob, parameters);
            log.info("✅ [정산 Job 실행] host={}, lot={}, cycle={}, 기간=({} ~ {})",
                    target.getHostUuid(), target.getParkingLotUuid(), cycle, start, end);
        } catch (Exception e) {
            log.error("❌ [정산 Job 실행 실패] host={}, lot={}, cycle={}, error={}",
                    target.getHostUuid(), target.getParkingLotUuid(), cycle, e.getMessage(), e);
        }
    }

    private LocalDate getStartDate(YearMonth ym, SettlementCycle cycle) {
        return (cycle == SettlementCycle.FIFTEEN) ? ym.atDay(1) : ym.atDay(16);
    }

    private LocalDate getEndDate(YearMonth ym, SettlementCycle cycle) {
        return (cycle == SettlementCycle.FIFTEEN) ? ym.atDay(15) : ym.atEndOfMonth();
    }
}