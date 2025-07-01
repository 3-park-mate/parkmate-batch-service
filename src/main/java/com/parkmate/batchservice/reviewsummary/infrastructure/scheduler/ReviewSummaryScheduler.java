package com.parkmate.batchservice.reviewsummary.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSummaryScheduler {

    private final JobLauncher jobLauncher;
    private final Job reviewSummaryJob;

    @Scheduled(fixedDelay = 60000)
    public void runReviewSummaryJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("[리뷰 요약 배치] 실행 시작");

            jobLauncher.run(reviewSummaryJob, jobParameters);

            log.info("[리뷰 요약 배치] 실행 완료");
        } catch (Exception e) {
            log.error("[리뷰 요약 배치] 실행 중 오류 발생", e);
        }
    }
}