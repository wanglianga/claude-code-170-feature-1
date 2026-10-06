package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 后续服务记录：临时照护、送医陪诊、日常巡访、助餐、适老化改造、心理陪伴等。
 */
@Getter
@Setter
@Entity
@Table(name = "event_service")
public class EventService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /** DAILY_VISIT 日常巡访 / MEAL_SERVICE 助餐 / AGING_RETROFIT 适老化改造 /
     *  PSYCH_COMPANION 心理陪伴 / TEMP_CARE 临时照护 / HOSPITAL_ESCORT 送医陪诊 / OTHER 其他 */
    @Column(nullable = false)
    private String serviceType;

    @Column(nullable = false)
    private String providerName;
    private String providerPhone;

    @Column(length = 2000)
    private String content;

    /** PENDING 待服务 / IN_PROGRESS 进行中 / DONE 已完成 */
    @Column(nullable = false)
    private String status = "DONE";

    private LocalDateTime serviceAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}
