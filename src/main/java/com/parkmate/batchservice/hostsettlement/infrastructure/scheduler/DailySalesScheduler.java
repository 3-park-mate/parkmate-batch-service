package com.parkmate.batchservice.hostsettlement.infrastructure.scheduler;

import com.parkmate.batchservice.hostsettlement.application.HostSettlementService;
import com.parkmate.batchservice.hostsettlement.dto.request.HostParkingLotDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailySalesScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailySalesJob;
    private final HostSettlementService hostSettlementService;

    /**
     * 매일 자정 실행 (전일 매출 정산)
     */
    //@Scheduled(cron = "0 0 0 * * *") // 운영용
    @Scheduled(cron = "0 */1 * * * *") // 테스트용: 매분 실행
    public void executeDailySettlementJob() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        log.info("🚀 [일 정산 스케줄러] 시작 - 대상 날짜: {}", targetDate);

        List<HostParkingLotDto> targets = hostSettlementService.getAllHostParkingLotPairsFromPayment(targetDate);

        if (targets.isEmpty()) {
            log.info("📭 [일 정산 스케줄러] 대상 없음 - {}", targetDate);
            return;
        }

        for (HostParkingLotDto pair : targets) {
            JobParameters params = buildJobParameters(pair.getHostUuid(), pair.getParkingLotUuid(), targetDate);
            try {
                jobLauncher.run(dailySalesJob, params);
                log.info("✅ [정산 성공] host={}, lot={}, date={}", pair.getHostUuid(), pair.getParkingLotUuid(), targetDate);
            } catch (Exception e) {
                log.error("❌ [정산 실패] host={}, lot={}, date={}, error={}",
                        pair.getHostUuid(), pair.getParkingLotUuid(), targetDate, e.getMessage(), e);
            }
        }

        log.info("🏁 [일 정산 스케줄러] 종료");
    }

    private JobParameters buildJobParameters(String hostUuid, String parkingLotUuid, LocalDate targetDate) {
        return new JobParametersBuilder()
                .addString("hostUuid", hostUuid)
                .addString("parkingLotUuid", parkingLotUuid)
                .addString("targetDate", targetDate.toString())
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
    }
}