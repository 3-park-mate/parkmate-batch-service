package com.parkmate.batchservice.hostsettlement.infrastructure.writer;

import com.parkmate.batchservice.hostsettlement.domain.HostSettlement;
import com.parkmate.batchservice.hostsettlement.infrastructure.repository.HostSettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MonthlySalesWriter implements ItemWriter<HostSettlement> {

    private final HostSettlementRepository hostSettlementRepository;

    @Override
    public void write(Chunk<? extends HostSettlement> chunk) {
        if (chunk.isEmpty()) {
            log.info("📦 [Writer] 정산 항목이 없습니다.");
            return;
        }

        List<HostSettlement> filteredSettlements = filterDuplicates(chunk.getItems());

        if (filteredSettlements.isEmpty()) {
            log.info("⚠️ [Writer] 저장할 정산 항목이 없습니다. (모두 중복)");
            return;
        }

        saveSettlements(filteredSettlements);
    }

    private List<HostSettlement> filterDuplicates(List<? extends HostSettlement> settlements) {
        return settlements.stream()
                .map(settlement -> (HostSettlement) settlement) // 명시적 캐스팅
                .filter(settlement -> {
                    boolean exists = isDuplicate(settlement);
                    if (exists) {
                        logDuplicate(settlement);
                    }
                    return !exists;
                })
                .collect(Collectors.toList()); // toList() 대신 collect 사용
    }

    private boolean isDuplicate(HostSettlement settlement) {
        try {
            return hostSettlementRepository.existsByHostUuidAndParkingLotUuidAndSettlementDateAndSettlementCycle(
                    settlement.getHostUuid(),
                    settlement.getParkingLotUuid(),
                    settlement.getSettlementDate(),
                    settlement.getSettlementCycle()
            );
        } catch (Exception e) {
            log.error("❌ [Writer] 중복 검사 실패 - {}", e.getMessage(), e);
            return true;
        }
    }

    private void logDuplicate(HostSettlement settlement) {
        log.warn("🚫 [Writer] 중복 정산 생략 - host={}, lot={}, date={}, cycle={}",
                settlement.getHostUuid(),
                settlement.getParkingLotUuid(),
                settlement.getSettlementDate(),
                settlement.getSettlementCycle());
    }

    private void saveSettlements(List<HostSettlement> settlements) {
        try {
            hostSettlementRepository.saveAll(settlements);
            log.info("✅ [Writer] 정산 저장 완료 - {}건", settlements.size());
        } catch (Exception e) {
            log.error("❌ [Writer] 정산 저장 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}