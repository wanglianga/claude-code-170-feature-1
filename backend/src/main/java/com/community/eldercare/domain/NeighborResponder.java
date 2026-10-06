package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 邻里响应人 / 志愿者
 */
@Getter
@Setter
@Entity
@Table(name = "neighbor_responder")
public class NeighborResponder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @Column(nullable = false)
    private String name;

    private String phone;
    /** 与老人位置关系，如 同单元302、对门 */
    private String proximity;
    private boolean active = true;

    /** 协助可信度评分 0-100（代报信息清晰、到场及时等） */
    private Integer reliabilityScore = 80;

    /** 累计应呼叫次数 / 实际有效响应次数 */
    private Integer callCount = 0;
    private Integer effectiveCount = 0;

    @Column(length = 500)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
