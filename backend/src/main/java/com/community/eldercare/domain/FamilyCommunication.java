package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 家属沟通记录。多次未接通是“家属长期不接电话”风险判定的依据。
 */
@Getter
@Setter
@Entity
@Table(name = "event_family_comm")
public class FamilyCommunication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    private EmergencyContact contact;

    @Column(nullable = false)
    private String contactName;
    private String contactPhone;

    /** 沟通方式：电话/短信/微信/当面 */
    @Column(nullable = false)
    private String channel = "电话";

    /** 是否接通 */
    private boolean reached;
    /** 家属响应态度：积极配合/一般/推诿拒绝/未接通 */
    private String attitude;

    private String operatorName;
    private LocalDateTime communicatedAt = LocalDateTime.now();

    @Column(length = 2000)
    private String note;
}
