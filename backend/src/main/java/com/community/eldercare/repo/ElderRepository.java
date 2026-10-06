package com.community.eldercare.repo;

import com.community.eldercare.domain.Elder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElderRepository extends JpaRepository<Elder, Long> {
    List<Elder> findByNameContainingOrderByIdDesc(String name);
    List<Elder> findAllByOrderByUpdatedAtDesc();
    List<Elder> findByRiskLevelOrderByUpdatedAtDesc(String riskLevel);
}
