package com.parkmate.batchservice.hostsettlement.infrastructure.job;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.infrastructure.client.PaymentFeignClient;
import com.parkmate.batchservice.hostsettlement.infrastructure.reader.MonthlySalesReader;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MonthlySalesConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HostSettlementRepository hostSettlementRepository;
    private final PaymentFeignClient paymentFeignClient;

    @Bean
    public Job monthlySalesJob(Step monthlySalesStep) {
        return new JobBuilder("monthlySalesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(monthlySalesStep)
                .build();
    }

    @Bean
    public Step monthlySalesStep(ItemReader<HostSettlement> monthlySalesReader,
                                 ItemWriter<HostSettlement> monthlySalesWriter) {
        return new StepBuilder("monthlySalesStep", jobRepository)
                .<HostSettlement, HostSettlement>chunk(1, transactionManager)
                .reader(monthlySalesReader)
                .writer(monthlySalesWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<HostSettlement> monthlySalesReader(
            @Value("#{jobParameters['hostUuid']}") String hostUuid,
            @Value("#{jobParameters['parkingLotUuid']}") String parkingLotUuid,
            @Value("#{jobParameters['year']}") int year,
            @Value("#{jobParameters['month']}") int month,
            @Value("#{jobParameters['settlementCycle']}") String cycleName // 직접 enum name
    ) {
        SettlementCycle settlementCycle = SettlementCycle.valueOf(cycleName); // 직접 enum 변환
        return new MonthlySalesReader(paymentFeignClient, hostUuid, parkingLotUuid, year, month, settlementCycle);
    }

    @Bean
    @StepScope
    public ItemWriter<HostSettlement> monthlySalesWriter() {
        return new MonthlySalesWriter(hostSettlementRepository);
    }
}