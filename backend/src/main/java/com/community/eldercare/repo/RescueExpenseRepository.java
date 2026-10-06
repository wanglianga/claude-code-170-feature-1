package com.community.eldercare.repo;

import com.community.eldercare.domain.RescueExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RescueExpenseRepository extends JpaRepository<RescueExpense, Long> {
    List<RescueExpense> findByRescueIdOrderByIdAsc(Long rescueId);
}
