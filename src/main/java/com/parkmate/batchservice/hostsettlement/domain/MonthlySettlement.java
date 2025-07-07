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
@Table(name = "monthly_settlement")
public class MonthlySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("정산 코드")
    @Column(nullable = false, unique = true, length = 100)
    private String settlementCode;

    @Comment("호스트 UUID")
    @Column(nullable = false, length = 36)
    private String hostUuid;

    @Comment("주차장 UUID")
    @Column(nullable = false, length = 36)
    private String parkingLotUuid;

    @Comment("정산 일자(집계 마지막일)")
    @Column(nullable = false)
    private LocalDate settlementDate;

    @Comment("총 매출 금액")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Comment("정산 상태")
    @Column(nullable = false, length = 20)
    private String status;

    @Comment("정산 주기")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SettlementCycle settlementCycle;

    @Builder
    private MonthlySettlement(String settlementCode,
                              String hostUuid,
                              String parkingLotUuid,
                              LocalDate settlementDate,
                              BigDecimal totalAmount,
                              String status,
                              SettlementCycle settlementCycle) {
        this.settlementCode = settlementCode;
        this.hostUuid = hostUuid;
        this.parkingLotUuid = parkingLotUuid;
        this.settlementDate = settlementDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.settlementCycle = settlementCycle;
    }
} 