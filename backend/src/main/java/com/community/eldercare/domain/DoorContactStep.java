package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 无法开门多方联系步骤：按 家属 → 物业 → 邻居 → 派出所 顺序登记，
 * 每一步记录联系人、联系方式、联系时间与反馈结果。
 */
@Getter
@Setter
@Entity
@Table(name = "door_contact_step", indexes = {
        @Index(name = "idx_door_step_esc", columnList = "escalation_id")
})
public class DoorContactStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DoorEscalation escalation;

    /** FAMILY 家属 / PROPERTY 物业 / NEIGHBOR 邻居 / POLICE 派出所 */
    @Column(nullable = false)
    private String partyType;

    @Column(nullable = false)
    private String contactName;
    private String contactPhone;

    /** 联系方式：电话/上门/微信/对讲机 等 */
    private String channel = "电话";

    /** 是否联系上 */
    private boolean reached;

    /** 对方反馈：如“儿子在外地无法赶回”“物业无备用钥匙”“邻居有钥匙 5 分钟到”“民警建议破门并到场见证” */
    @Column(length = 1000)
    private String feedback;

    /** 对方是否会到场协助 */
    private boolean willComeToScene;

    private String operatorName;
    private LocalDateTime contactedAt = LocalDateTime.now();

    public Long getEscalationId() {
        return escalation != null ? escalation.getId() : null;
    }
}
