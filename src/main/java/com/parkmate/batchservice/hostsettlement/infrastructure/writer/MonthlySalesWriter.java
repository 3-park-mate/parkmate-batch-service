package com.parkmate.batchservice.hostsettlement.infrastructure.writer;

import com.parkmate.batchservice.hostsettlement.domain.MonthlySettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.MonthlySettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MonthlySalesWriter implements ItemWriter<MonthlySettlement> {
    private final MonthlySettlementRepository monthlySettlementRepository;

    @Override
    public void write(Chunk<? extends MonthlySettlement> chunk) {
        if (chunk.isEmpty()) {
            log.info("📦 [MonthlySalesWriter] 저장할 월매출 데이터가 없습니다.");
            return;
        }
        List<? extends MonthlySettlement> settlements = chunk.getItems();
        List<MonthlySettlement> newSettlements = settlements.stream()
                .filter(s -> !monthlySettlementRepository.existsBySettlementCode(s.getSettlementCode()))
                .collect(Collectors.toList());
        if (newSettlements.isEmpty()) {
            log.info("⚠️ [MonthlySalesWriter] 모두 중복 settlementCode, 저장 생략");
            return;
        }
        monthlySettlementRepository.saveAll(newSettlements);
        log.info("✅ [MonthlySalesWriter] 월매출 저장 완료 - {}건", newSettlements.size());
    }
}