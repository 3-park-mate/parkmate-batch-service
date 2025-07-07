package com.parkmate.batchservice.hostsettlement.infrastructure.job;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.domain.SettlementCycle;
import com.parkmate.batchservice.hostsettlement.infrastructure.processor.MonthlySalesProcessor;
import com.parkmate.batchservice.hostsettlement.infrastructure.reader.MonthlySalesReader;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.MonthlySettlementRepository;
import com.parkmate.batchservice.hostsettlement.infrastructure.writer.MonthlySalesWriter;
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
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MonthlySalesConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailySettlementRepository dailySettlementRepository;
    private final MonthlySettlementRepository monthlySettlementRepository;

    @Bean
    public Job monthlySalesJob(Step monthlySalesStep) {
        return new JobBuilder("monthlySalesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(monthlySalesStep)
                .build();
    }

    @Bean
    public Step monthlySalesStep(ItemReader<List<DailySettlement>> monthlySalesReader,
                                 ItemProcessor<List<DailySettlement>, MonthlySettlement> monthlySalesProcessor,
                                 ItemWriter<MonthlySettlement> monthlySalesWriter) {
        return new StepBuilder("monthlySalesStep", jobRepository)
                .<List<DailySettlement>, MonthlySettlement>chunk(100, transactionManager)
                .reader(monthlySalesReader)
                .processor(monthlySalesProcessor)
                .writer(monthlySalesWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<List<DailySettlement>> monthlySalesReader(
            @Value("#{jobParameters['hostUuid']}") String hostUuid,
            @Value("#{jobParameters['parkingLotUuid']}") String parkingLotUuid,
            @Value("#{jobParameters['start']}") String startStr,
            @Value("#{jobParameters['end']}") String endStr,
            @Value("#{jobParameters['settlementCycle']}") String cycleStr
    ) {
        LocalDate start = LocalDate.parse(startStr);
        LocalDate end = LocalDate.parse(endStr);
        SettlementCycle cycle = SettlementCycle.valueOf(cycleStr);
        return new MonthlySalesReader(dailySettlementRepository, hostUuid, parkingLotUuid, start, end, cycle);
    }

    @Bean
    @StepScope
    public ItemProcessor<List<DailySettlement>, MonthlySettlement> monthlySalesProcessor(
            @Value("#{jobParameters['settlementCycle']}") String cycleStr
    ) {
        SettlementCycle cycle = SettlementCycle.valueOf(cycleStr);
        return new MonthlySalesProcessor(cycle);
    }

    @Bean
    public ItemWriter<MonthlySettlement> monthlySalesWriter() {
        return new MonthlySalesWriter(monthlySettlementRepository);
    }
}