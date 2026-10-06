package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户：值班社工 / 网格员 / 邻里志愿者 / 家庭医生 / 社区管理者
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    /** DUTY_WORKER 值班社工 / GRID_WORKER 网格员 / NEIGHBOR_VOLUNTEER 邻里志愿者 / FAMILY_DOCTOR 家庭医生 / ADMIN 社区管理者 */
    @Column(nullable = false)
    private String role;

    private String phone;
    private String community;
    private boolean enabled = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
