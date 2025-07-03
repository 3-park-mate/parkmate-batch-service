package com.parkmate.batchservice.hostsettlement.domain;

import com.parkmate.batchservice.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "host_settlement")
public class HostSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("호스트 UUID")
    @Column(nullable = false, length = 36)
    private String hostUuid;

    @Comment("주차장 UUID")
    @Column(nullable = false, length = 36)
    private String parkingLotUuid;

    @Comment("정산 기준 일자")
    @Column(nullable = false)
    private LocalDate settlementDate;

    @Comment("총 매출 금액")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSalesAmount;

    @Comment("정산 상태 (예: PENDING, COMPLETED)")
    @Column(nullable = false, length = 20)
    private String status;

    @Comment("정산 주기 (FIFTEEN: 15일마다, THIRTY: 30일마다)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SettlementCycle settlementCycle;

    @Builder
    private HostSettlement(String hostUuid,
                           String parkingLotUuid,
                           LocalDate settlementDate,
                           BigDecimal totalSalesAmount,
                           String status,
                           SettlementCycle settlementCycle) {
        this.hostUuid = hostUuid;
        this.parkingLotUuid = parkingLotUuid;
        this.settlementDate = settlementDate;
        this.totalSalesAmount = totalSalesAmount;
        this.status = status;
        this.settlementCycle = settlementCycle;
    }
}