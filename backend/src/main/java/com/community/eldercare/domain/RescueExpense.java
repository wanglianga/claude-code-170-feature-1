package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 破门救助相关费用项：锁匠费用、锁具更换、门窗修复、临时安置等。
 * 每笔费用记录垫付方与最终承担方（家庭承担 / 社区救助 / 保险理赔 / 待定），
 * 供家属到场后在完整处置轨迹中逐项确认，避免事后费用争议。
 */
@Getter
@Setter
@Entity
@Table(name = "event_rescue_expense")
public class RescueExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DoorBreakRescue rescue;

    /** 费用项目：开锁费/换锁费/门体修复/窗户修复/临时陪护/其他 */
    @Column(nullable = false)
    private String itemType;

    @Column(length = 500)
    private String itemDetail;

    @Column(nullable = false)
    private BigDecimal amount;

    /** 垫付方：社区/物业/家属/施工方挂账 */
    private String advancedBy;

    /**
     * 最终承担方：FAMILY 家庭承担 / COMMUNITY_AID 社区救助 /
     * INSURANCE 保险理赔 / PENDING 待家属确认
     */
    @Column(nullable = false)
    private String bearer = "PENDING";

    /** 保险/救助单号（进入理赔或社区救助流程时填写） */
    private String claimNo;

    /** 票据/报价单/理赔凭证地址（多个逗号分隔） */
    @Column(length = 2000)
    private String receiptUrls;

    private LocalDateTime createdAt = LocalDateTime.now();

    /** 家属是否已确认该笔费用承担方式 */
    private boolean familyConfirmed;
}
