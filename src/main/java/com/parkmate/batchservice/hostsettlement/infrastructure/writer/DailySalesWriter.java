package com.parkmate.batchservice.hostsettlement.infrastructure.writer;

import com.parkmate.batchservice.hostsettlement.domain.DailySettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.DailySettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DailySalesWriter implements ItemWriter<DailySettlement> {
    private final DailySettlementRepository dailySettlementRepository;

    @Override
    public void write(Chunk<? extends DailySettlement> chunk) {
        if (chunk.isEmpty()) {
            log.info("📦 [DailySalesWriter] 저장할 일매출 데이터가 없습니다.");
            return;
        }
        List<? extends DailySettlement> settlements = chunk.getItems();
        List<DailySettlement> newSettlements = settlements.stream()
                .filter(s -> !dailySettlementRepository.existsByReservationCode(s.getReservationCode()))
                .collect(Collectors.toList());
        if (newSettlements.isEmpty()) {
            log.info("⚠️ [DailySalesWriter] 모두 중복 reservationCode, 저장 생략");
            return;
        }
        dailySettlementRepository.saveAll(newSettlements);
        log.info("✅ [DailySalesWriter] 일매출 저장 완료 - {}건", newSettlements.size());
    }
}