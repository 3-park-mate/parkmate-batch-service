package com.parkmate.batchservice.reviewsummary.infrastructure.job;

import com.parkmate.batchservice.kafka.event.ReviewCreatedJoinUserEvent;
import com.parkmate.batchservice.reviewsummary.domain.ReviewSummary;
import com.parkmate.batchservice.reviewsummary.infrastructure.processor.ReviewSummaryProcessor;
import com.parkmate.batchservice.reviewsummary.infrastructure.reader.ReviewSummaryReader;
import com.parkmate.batchservice.reviewsummary.infrastructure.writer.ReviewSummaryWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ReviewSummaryJobConfig {

    private final ReviewSummaryReader reviewSummaryReader;
    private final ReviewSummaryProcessor reviewSummaryProcessor;
    private final ReviewSummaryWriter reviewSummaryWriter;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job reviewSummaryJob() {
        return new JobBuilder("reviewSummaryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(reviewSummaryStep())
                .build();
    }

    @Bean
    public Step reviewSummaryStep() {
        return new StepBuilder("reviewSummaryStep", jobRepository)
                .<ReviewCreatedJoinUserEvent, ReviewSummary>chunk(100, transactionManager)
                .reader(reviewSummaryReader)
                .processor(reviewSummaryProcessor)
                .writer(reviewSummaryWriter)
                .build();
    }
}