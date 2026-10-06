package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 回访记录。事件不停在处置结果，回访后还要更新画像并制定下一次主动关怀计划。
 */
@Getter
@Setter
@Entity
@Table(name = "event_follow_up")
public class EventFollowUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /** IMPROVED 好转 / STABLE 平稳 / WORSENED 恶化 / NO_ANSWER 未联系上 */
    @Column(nullable = false)
    private String result;

    /** 回访方式：电话回访/上门回访/小程序 */
    @Column(nullable = false)
    private String method = "电话回访";

    /** 满意度 1-5 */
    private Integer satisfaction;

    /** 下一次主动关怀类型：日常巡访/助餐/适老化改造/心理陪伴 */
    private String nextCareType;
    /** 下一次主动关怀时间 */
    private LocalDateTime nextCareAt;

    private String operatorName;
    private LocalDateTime followedUpAt = LocalDateTime.now();

    @Column(length = 2000)
    private String note;
}
