package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 破门 / 请锁匠处置记录。
 * 保存授权依据、现场见证人、执行人，以及救助完成后的
 * 锁具更换、门窗修复、物业见证与老人后续安置，避免责任争议。
 */
@Getter
@Setter
@Entity
@Table(name = "forced_entry")
public class ForcedEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private DoorEscalation escalation;

    /** LOCKSMITH 请锁匠开锁 / FORCED_ENTRY 破门进入 */
    @Column(nullable = false)
    private String method;

    /* ---------------- 授权依据 ---------------- */

    /** FAMILY_PHONE 家属电话授权 / FAMILY_ONSITE 家属现场授权 /
     *  POLICE_ONSITE 民警现场授权 / COMMUNITY_EMERGENCY 社区应急预案授权 */
    @Column(nullable = false)
    private String authorizationBasis;

    /** 授权依据说明：如通话录音编号、授权时间、民警警号、应急预案条款 */
    @Column(length = 1000)
    private String authorizationDetail;

    /** 授权人姓名 */
    @Column(nullable = false)
    private String authorizedBy;
    private LocalDateTime authorizedAt;

    /* ---------------- 执行与见证 ---------------- */

    /** 执行人（锁匠/施工人员） */
    @Column(nullable = false)
    private String executorName;
    private String executorPhone;
    private LocalDateTime executedAt;

    /** 现场见证人（多人逗号分隔，必填） */
    @Column(nullable = false, length = 500)
    private String witnessNames;

    /** 物业见证人 */
    private String propertyWitnessName;
    /** 民警见证人（警号） */
    private String policeWitnessName;

    /** 门锁/门窗损坏情况 */
    @Column(length = 1000)
    private String damageDescription;

    /* ---------------- 后续修复与安置 ---------------- */

    /** 锁具是否已更换 */
    private boolean lockReplaced;
    @Column(length = 500)
    private String lockReplaceNote;

    /** 门窗是否已修复 */
    private boolean doorWindowRepaired;
    @Column(length = 500)
    private String repairNote;

    /** 老人后续安置：HOME 家中留观 / HOSPITAL 送医观察 / FAMILY_CARE 家属接回照顾 / TEMP_CARE 社区临时照护点 */
    private String elderPlacement;
    @Column(length = 1000)
    private String placementNote;

    private String createdByName;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getEscalationId() {
        return escalation != null ? escalation.getId() : null;
    }
}
