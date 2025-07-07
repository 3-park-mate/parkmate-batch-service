package com.parkmate.batchservice.hostsettlement.infrastructure.job;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.infrastructure.processor.DailySalesProcessor;
import com.parkmate.batchservice.hostsettlement.infrastructure.reader.DailySalesReader;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.writer.DailySalesWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DailySalesConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailySettlementRepository dailySettlementRepository;

    /**
     * 하루 매출 정산 Job 구성
     */
    @Bean
    public Job dailySalesJob(Step dailySalesStep) {
        return new JobBuilder("dailySalesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(dailySalesStep)
                .build();
    }

    /**
     * Step 구성: Reader → Processor → Writer
     */
    @Bean
    public Step dailySalesStep(ItemReader<DailySettlement> dailySalesReader,
                               ItemProcessor<DailySettlement, DailySettlement> dailySalesProcessor,
                               ItemWriter<DailySettlement> dailySalesWriter) {
        return new StepBuilder("dailySalesStep", jobRepository)
                .<DailySettlement, DailySettlement>chunk(100, transactionManager)
                .reader(dailySalesReader)
                .processor(dailySalesProcessor)
                .writer(dailySalesWriter)
                .build();
    }

    /**
     * DailySettlement Reader
     */
    @Bean
    @StepScope
    public ItemReader<DailySettlement> dailySalesReader(
            @Value("#{jobParameters['hostUuid']}") String hostUuid,
            @Value("#{jobParameters['parkingLotUuid']}") String parkingLotUuid,
            @Value("#{jobParameters['jobDate']}") String jobDateStr
    ) {
        LocalDate settlementDate = LocalDate.parse(jobDateStr);
        return new DailySalesReader(dailySettlementRepository, hostUuid, parkingLotUuid, settlementDate);
    }

    /**
     * DailySettlement Processor
     */
    @Bean
    @StepScope
    public ItemProcessor<DailySettlement, DailySettlement> dailySalesProcessor() {
        return new DailySalesProcessor();
    }

    /**
     * DailySettlement Writer
     */
    @Bean
    public ItemWriter<DailySettlement> dailySalesWriter() {
        return new DailySalesWriter(dailySettlementRepository);
    }
}