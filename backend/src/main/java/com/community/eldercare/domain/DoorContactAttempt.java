package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 无法开门时的多方联系确认记录。
 * 平台引导值班人员依次联系：家属 FAMILY → 物业 PROPERTY → 邻居 NEIGHBOR → 派出所 POLICE，
 * 每一步均留存联系人、时间、反馈与是否承诺到场，作为后续破门/锁匠救助的授权链依据。
 */
@Getter
@Setter
@Entity
@Table(name = "event_door_contact", indexes = {
        @Index(name = "idx_door_contact_event", columnList = "event_id")
})
public class DoorContactAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /** 联系顺序（1 起，按家属→物业→邻居→派出所的引导次序） */
    @Column(nullable = false)
    private Integer seq;

    /** FAMILY 家属 / PROPERTY 物业 / NEIGHBOR 邻居 / POLICE 派出所 */
    @Column(nullable = false)
    private String partyType;

    @Column(nullable = false)
    private String partyName;
    private String partyPhone;

    /** 联系方式：电话/短信/微信/上门/对讲 */
    private String channel = "电话";

    private LocalDateTime contactedAt = LocalDateTime.now();

    /** 是否联系上 */
    private boolean reached;

    /** 反馈内容：无人接听/稍后送钥匙/同意破门/拒绝承担/民警将到场 等 */
    @Column(length = 1000)
    private String feedback;

    /** 是否承诺到场开门/协助 */
    private Boolean willArrive;
    /** 承诺到场所需时间（分钟） */
    private Integer etaMinutes;

    private String operatorName;

    @Column(length = 1000)
    private String note;
}
