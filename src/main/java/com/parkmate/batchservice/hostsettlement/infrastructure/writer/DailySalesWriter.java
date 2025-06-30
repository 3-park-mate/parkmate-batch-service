package com.parkmate.batchservice.hostsettlement.infrastructure.writer;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.HostSettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class DailySalesWriter implements ItemWriter<HostSettlement> {

    private final HostSettlementRepository repository;

    @Override
    public void write(Chunk<? extends HostSettlement> chunk) {
        if (chunk.isEmpty()) {
            log.info("💤 [Writer] 정산 항목 없음 - 저장 생략");
            return;
        }

        try {
            repository.saveAll(chunk.getItems());
            log.info("✅ [Writer] 정산 저장 완료 - {}건", chunk.size());

            chunk.getItems().forEach(settlement ->
                    log.debug("📌 저장 완료: host={}, lot={}, date={}, amount={}",
                            settlement.getHostUuid(),
                            settlement.getParkingLotUuid(),
                            settlement.getSettlementDate(),
                            settlement.getTotalSalesAmount()));
        } catch (Exception e) {
            log.error("❌ [Writer] 정산 저장 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}