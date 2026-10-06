package com.community.eldercare.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 破门/锁匠救助的现场见证人（物业、民警、邻居、社区工作人员、家属等）。
 * 见证人记录与授权依据共同构成救助合法性的留痕，避免救助后责任争议。
 */
@Getter
@Setter
@Entity
@Table(name = "event_door_witness")
public class DoorBreakWitness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private DoorBreakRescue rescue;

    @Column(nullable = false)
    private String name;

    /** 身份：物业工作人员/民警/邻居/社区工作者/家属 等 */
    @Column(nullable = false)
    private String role;

    private String phone;

    /** 现场签字/合影/证件凭证地址 */
    private String evidenceUrl;
}
