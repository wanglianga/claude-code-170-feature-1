package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 破门/锁匠救助记录（无法开门且多方确认后采取的强制入户救助）。
 * 留存：采取方式、授权依据、现场见证人、锁具更换/门窗修复、费用垫付与老人后续安置，
 * 以及家属到场后对处置轨迹与费用承担的确认，形成完整责任链。
 */
@Getter
@Setter
@Entity
@Table(name = "event_door_break_rescue")
public class DoorBreakRescue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /* ---------------- 处置方式 ---------------- */

    /** BREAK_DOOR 破门 / LOCKSMITH 请锁匠 / BOTH 破门并换锁 */
    @Column(nullable = false)
    private String actionType;

    /** 锁匠/施工方名称与电话 */
    private String providerName;
    private String providerPhone;

    private LocalDateTime rescuedAt = LocalDateTime.now();
    private String operatorName;

    /* ---------------- 授权依据 ---------------- */

    /**
     * 授权类型：FAMILY_AUTHORIZED 家属电话/微信授权 / POLICE_ORDER 民警现场决定 /
     * COMMUNITY_EMERGENCY 社区紧急避险决定 / OTHER 其他
     */
    @Column(nullable = false)
    private String authorizationType;

    /** 授权人姓名（如家属、民警） */
    private String authorizerName;
    private String authorizerPhone;

    /** 授权时间 */
    private LocalDateTime authorizedAt;

    /**
     * 授权凭证：电话录音/微信截图/民警执法记录号/社区紧急会议纪要 等文件地址（多个逗号分隔）。
     * 系统强制要求授权依据，未填写不能登记破门救助。
     */
    @Column(length = 2000, nullable = false)
    private String authorizationEvidence;

    /** 授权原话/决定内容与紧急避险理由（老人无响应、屋内有灯光、疑似生命危险等） */
    @Column(length = 2000)
    private String authorizationNote;

    /* ---------------- 现场情况与修复 ---------------- */

    /** 破拆部位与现场情况描述 */
    @Column(length = 1000)
    private String sceneDescription;

    /** 现场照片/视频地址（多个逗号分隔） */
    @Column(length = 2000)
    private String scenePhotoUrls;

    /** 是否更换锁具 */
    private boolean lockReplaced;
    private String lockModel;
    /** 新钥匙/门禁保管去向：家属/物业/社区/邻居 */
    private String newKeyKeptBy;

    /** 门窗修复安排 */
    @Column(length = 1000)
    private String repairArrangement;
    private LocalDateTime repairedAt;

    /* ---------------- 费用与安置 ---------------- */

    /** 费用垫付方：社区/物业/家属/施工方挂账 */
    private String advancedBy;
    private java.math.BigDecimal advancedAmount;

    /** 老人后续安置：送医/临时照护点/家属接走/邻居暂照护/居家（已恢复安全） */
    private String elderPlacement;
    @Column(length = 1000)
    private String placementNote;

    /** 物业是否已见证并登记 */
    private boolean propertyWitnessed;
    private String propertyWitnessName;

    /* ---------------- 家属到场确认 ---------------- */

    /** 家属是否已到场查看完整处置轨迹 */
    private boolean familyConfirmed;
    private String familyConfirmerName;
    private LocalDateTime familyConfirmedAt;
    /** 家属确认意见（认可/异议及说明） */
    @Column(length = 1000)
    private String familyConfirmNote;
    /** 家属确认签字/合影凭证 */
    private String familyConfirmEvidence;

    @OneToMany(mappedBy = "rescue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DoorBreakWitness> witnesses = new ArrayList<>();

    @OneToMany(mappedBy = "rescue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RescueExpense> expenses = new ArrayList<>();
}
