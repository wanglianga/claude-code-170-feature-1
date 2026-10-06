package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorContactStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoorContactStepRepository extends JpaRepository<DoorContactStep, Long> {
    List<DoorContactStep> findByEscalation_IdOrderByContactedAtAsc(Long escalationId);
}
