package com.parkmate.batchservice.hostsettlement.infrastructure.processor;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import org.springframework.batch.item.ItemProcessor;

public class DailySalesProcessor implements ItemProcessor<DailySettlement, DailySettlement> {
    @Override
    public DailySettlement process(DailySettlement item) {
        // 이미 DailySettlement로 저장된 데이터이므로 추가 가공 없이 그대로 반환
        return item;
    }
}