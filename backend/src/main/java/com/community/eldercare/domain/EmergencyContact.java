package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 紧急联系人（家属）
 */
@Getter
@Setter
@Entity
@Table(name = "emergency_contact")
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @Column(nullable = false)
    private String name;

    private String relationship;
    private String phone;
    private boolean primaryContact;

    /** 是否长期不接电话 */
    private boolean oftenUnreachable;

    /** 历史接通率备注 */
    @Column(length = 500)
    private String reachabilityNote;

    /** 累计联系次数 / 接通次数，用于家属响应表现评估 */
    private Integer contactAttempts = 0;
    private Integer contactReached = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}
