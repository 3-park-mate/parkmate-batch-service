package com.parkmate.batchservice.hostsettlement.infrastructure.job;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.client.PaymentFeignClient;
import com.parkmate.batchservice.hostsettlement.infrastructure.reader.DailySalesReader;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.HostSettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.writer.MonthlySalesWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DailySalesConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HostSettlementRepository hostSettlementRepository;
    private final PaymentFeignClient paymentFeignClient;

    @Bean
    public Job dailySalesJob(Step dailySalesStep) {
        return new JobBuilder("dailySalesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(dailySalesStep)
                .build();
    }

    @Bean
    public Step dailySalesStep(ItemReader<HostSettlement> dailySalesReader,
                               ItemWriter<HostSettlement> dailySalesWriter) {
        return new StepBuilder("dailySalesStep", jobRepository)
                .<HostSettlement, HostSettlement>chunk(1, transactionManager)
                .reader(dailySalesReader)
                .writer(dailySalesWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<HostSettlement> dailySalesReader(
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['hostUuid']}") String hostUuid,
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['parkingLotUuid']}") String parkingLotUuid,
            @org.springframework.beans.factory.annotation.Value("#{jobParameters['targetDate']}") String targetDateStr) {

        return new DailySalesReader(paymentFeignClient, hostUuid, parkingLotUuid, targetDateStr);
    }

    @Bean
    public ItemWriter<HostSettlement> dailySalesWriter() {
        return new MonthlySalesWriter(hostSettlementRepository);
    }
}