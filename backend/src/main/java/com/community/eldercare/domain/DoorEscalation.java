package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 无法开门多方确认处置单。
 * 社工到达后老人无响应（如屋内仍有灯光）时启动，平台引导值班人员
 * 依次联系 家属 → 物业 → 邻居 → 派出所，全程留痕；
 * 最终可能由某方开门结案，或升级为锁匠/破门救助。
 */
@Getter
@Setter
@Entity
@Table(name = "door_escalation", indexes = {
        @Index(name = "idx_door_esc_event", columnList = "event_id")
})
public class DoorEscalation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /** 关联的上门核实记录（未能入户那次），可空 */
    private Long onsiteVisitId;

    /* ---------------- 现场情况 ---------------- */

    /** 老人无响应（敲门/呼喊/电话均无回应） */
    private boolean elderNoResponse = true;
    /** 屋内有灯光/电视声等有人迹象 */
    private boolean lightOnInside = true;
    /** 现场情况补充说明 */
    @Column(length = 1000)
    private String sceneNote;

    /* ---------------- 状态 ---------------- */

    /** CONTACTING 多方联系中 / RESOLVED 已结案 */
    @Column(nullable = false)
    private String status = "CONTACTING";

    /** 结案方式：FAMILY_OPENED 家属到场开门 / PROPERTY_OPENED 物业协助开门 /
     *  NEIGHBOR_OPENED 邻居协助开门 / LOCKSMITH 锁匠开锁 / FORCED_ENTRY 破门救助 */
    private String resolution;

    @Column(length = 1000)
    private String resolveNote;

    private String startedByName;
    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    public Long getEventId() {
        return event != null ? event.getId() : null;
    }
}
