package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorBreakWitness;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoorBreakWitnessRepository extends JpaRepository<DoorBreakWitness, Long> {
    List<DoorBreakWitness> findByRescueIdOrderByIdAsc(Long rescueId);
}
