package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import org.springframework.batch.item.ItemProcessor;

public class DailySalesProcessor implements ItemProcessor<DailySettlement, DailySettlement> {
    @Override
    public DailySettlement process(DailySettlement item) {

        return item;
    }
}