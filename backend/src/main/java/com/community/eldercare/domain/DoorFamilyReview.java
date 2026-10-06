package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 家属到场确认：家属到场后查看完整处置轨迹，
 * 确认费用承担方案（家庭承担 / 社区救助 / 保险理赔）并留下意见。
 */
@Getter
@Setter
@Entity
@Table(name = "door_family_review", indexes = {
        @Index(name = "idx_door_review_esc", columnList = "escalation_id")
})
public class DoorFamilyReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DoorEscalation escalation;

    @Column(nullable = false)
    private String familyName;
    private String relationship;
    private LocalDateTime arrivedAt = LocalDateTime.now();

    /** 是否已查看完整处置轨迹 */
    private boolean viewedTrajectory = true;

    /** 费用确认意见：AGREE 认可费用分担 / PARTIAL 部分认可 / DISAGREE 有异议 */
    @Column(nullable = false)
    private String costOpinion = "AGREE";

    @Column(length = 1000)
    private String comment;

    private String recordedByName;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getEscalationId() {
        return escalation != null ? escalation.getId() : null;
    }
}
