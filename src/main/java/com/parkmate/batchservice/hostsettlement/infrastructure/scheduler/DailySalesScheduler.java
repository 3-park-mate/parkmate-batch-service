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

    //@Scheduled(cron = "0 0 0 * * *") // 운영
    @Scheduled(cron = "0 */1 * * * *") // 테스트
    public void executeDailySettlementJob() {
        LocalDate today = LocalDate.now();

        log.info("🚀 [Scheduler] 일 매출 정산 Job 시작 - 대상일자: {}", today);

        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("jobDate", today.toString()) // 날짜 추가
                    .addLong("run.id", System.currentTimeMillis()) // 중복 방지용
                    .toJobParameters();

            jobLauncher.run(dailySalesJob, params);
            log.info("✅ [Scheduler] 일 매출 정산 Job 실행 성공 - 대상일자: {}", today);
        } catch (Exception e) {
            log.error("❌ [Scheduler] 일 매출 정산 Job 실행 실패 - 대상일자: {}, 에러: {}", today, e.getMessage(), e);
        }

        log.info("🏁 [Scheduler] 일 매출 정산 Job 종료 - 대상일자: {}", today);
    }
}