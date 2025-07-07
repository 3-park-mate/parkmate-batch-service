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
@Table(name = "daily_settlement")
public class DailySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("예약 코드")
    @Column(nullable = false, unique = true, length = 100)
    private String reservationCode;

    @Comment("호스트 UUID")
    @Column(nullable = false, length = 36)
    private String hostUuid;

    @Comment("주차장 UUID")
    @Column(nullable = false, length = 36)
    private String parkingLotUuid;

    @Comment("정산 일자")
    @Column(nullable = false)
    private LocalDate settlementDate;

    @Comment("매출 금액")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Comment("정산 상태")
    @Column(nullable = false, length = 20)
    private String status;

    @Comment("정산 주기")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SettlementCycle settlementCycle;

    @Builder
    private DailySettlement(String reservationCode,
                            String hostUuid,
                            String parkingLotUuid,
                            LocalDate settlementDate,
                            BigDecimal amount,
                            String status,
                            SettlementCycle settlementCycle) {
        this.reservationCode = reservationCode;
        this.hostUuid = hostUuid;
        this.parkingLotUuid = parkingLotUuid;
        this.settlementDate = settlementDate;
        this.amount = amount;
        this.status = status;
        this.settlementCycle = settlementCycle;
    }
} 