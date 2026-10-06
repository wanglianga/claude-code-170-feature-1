package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 转介/通知记录：值班社工根据严重程度通知
 * 网格员 GRID_WORKER / 邻里志愿者 NEIGHBOR_VOLUNTEER / 家庭医生 FAMILY_DOCTOR / 120 EMERGENCY_120。
 * 一条事件可同时多方转介。
 */
@Getter
@Setter
@Entity
@Table(name = "event_dispatch")
public class EventDispatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    @Column(nullable = false)
    private String targetType;
    @Column(nullable = false)
    private String targetName;
    private String targetPhone;

    /** 通知方式 电话/平台推送/短信 */
    private String notifyChannel = "电话";
    /** NOTIFIED 已通知 / ACCEPTED 已受理 / ARRIVED 已到场 / REJECTED 无法响应 */
    @Column(nullable = false)
    private String status = "NOTIFIED";

    /** 是否由邻居代报，代报信息是否清晰（不清晰触发风险标记） */
    private boolean neighborReported;
    private boolean neighborInfoClear = true;

    private String notifiedByName;
    private LocalDateTime notifiedAt = LocalDateTime.now();
    private LocalDateTime acceptedAt;
    private LocalDateTime arrivedAt;

    /** 是否有效响应（到场/受理），归档时用于邻里可信度评估 */
    private boolean effectiveResponse;

    @Column(length = 1000)
    private String note;
}
