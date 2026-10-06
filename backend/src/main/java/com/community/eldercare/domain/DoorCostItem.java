package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 破门/开锁费用明细：记录费用项目、金额、垫付方与最终承担方
 * （家庭承担 / 社区救助 / 保险理赔），供家属到场后逐项确认。
 */
@Getter
@Setter
@Entity
@Table(name = "door_cost_item", indexes = {
        @Index(name = "idx_door_cost_entry", columnList = "forced_entry_id")
})
public class DoorCostItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ForcedEntry forcedEntry;

    /** LOCKSMITH_FEE 开锁费 / LOCK_REPLACE 锁具更换 / DOOR_WINDOW_REPAIR 门窗修复 / OTHER 其他费用 */
    @Column(nullable = false)
    private String itemType;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 垫付方：COMMUNITY 社区垫付 / FAMILY 家属垫付 / PROPERTY 物业垫付 / NONE 未垫付 */
    @Column(nullable = false)
    private String advanceBy = "COMMUNITY";

    /** 承担方：PENDING 待确认 / FAMILY 家庭承担 / COMMUNITY_FUND 社区救助 / INSURANCE 保险理赔 */
    @Column(nullable = false)
    private String bearer = "PENDING";

    @Column(length = 500)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getForcedEntryId() {
        return forcedEntry != null ? forcedEntry.getId() : null;
    }
}
