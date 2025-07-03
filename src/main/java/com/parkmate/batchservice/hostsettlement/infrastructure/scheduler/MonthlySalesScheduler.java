package com.parkmate.batchservice.hostsettlement.infrastructure.scheduler;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.HostSettlementRepository;
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
    private final HostSettlementRepository hostSettlementRepository;

    // 테스트용: 1분마다 실행 (실제 운영 시 cron 변경)
    @Scheduled(cron = "0 */1 * * * *")
    public void runMidMonthJob() {
        log.info("🚀 [정산 스케줄러] [15일 정산] 시작");
        executeMonthlySettlement(SettlementCycle.FIFTEEN);
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void runEndOfMonthJob() {
        log.info("🚀 [정산 스케줄러] [말일 정산] 시작");
        executeMonthlySettlement(SettlementCycle.THIRTY);
    }

    private void executeMonthlySettlement(SettlementCycle cycle) {
        YearMonth yearMonth = YearMonth.now();
        LocalDate settlementDate = getSettlementDate(yearMonth, cycle);

        List<HostParkingLotDto> targets =
                hostSettlementService.getAllHostParkingLotPairsFromPayment(yearMonth, cycle);

        for (HostParkingLotDto target : targets) {
            boolean isDuplicate = hostSettlementRepository.existsByHostUuidAndParkingLotUuidAndSettlementDateAndSettlementCycle(
                    target.getHostUuid(),
                    target.getParkingLotUuid(),
                    settlementDate,
                    cycle
            );

            if (isDuplicate) {
                log.warn("🚫 [중복 정산 생략] host={}, lot={}, date={}, cycle={}",
                        target.getHostUuid(), target.getParkingLotUuid(), settlementDate, cycle.name());
                continue;
            }

            launchJob(target.getHostUuid(), target.getParkingLotUuid(), yearMonth, cycle);
        }

        log.info("✅ [정산 스케줄러] {} 기준 정산 작업 완료", cycle.name());
    }

    private LocalDate getSettlementDate(YearMonth yearMonth, SettlementCycle cycle) {
        return (cycle == SettlementCycle.FIFTEEN)
                ? yearMonth.atDay(15)
                : yearMonth.atEndOfMonth();
    }

    private void launchJob(String hostUuid, String parkingLotUuid, YearMonth yearMonth, SettlementCycle cycle) {
        JobParameters parameters = new JobParametersBuilder()
                .addString("hostUuid", hostUuid)
                .addString("parkingLotUuid", parkingLotUuid)
                .addLong("year", (long) yearMonth.getYear())
                .addLong("month", (long) yearMonth.getMonthValue())
                .addString("settlementCycle", cycle.name()) // enum 값도 전달
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(monthlySalesJob, parameters);
            log.info("✅ [정산 Job 실행] host={}, lot={}, cycle={}", hostUuid, parkingLotUuid, cycle.name());
        } catch (Exception e) {
            log.error("❌ [정산 Job 실행 실패] host={}, lot={}, cycle={}, error={}",
                    hostUuid, parkingLotUuid, cycle.name(), e.getMessage(), e);
        }
    }
}