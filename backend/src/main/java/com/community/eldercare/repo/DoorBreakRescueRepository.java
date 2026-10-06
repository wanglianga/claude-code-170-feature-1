package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorBreakRescue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoorBreakRescueRepository extends JpaRepository<DoorBreakRescue, Long> {
    Optional<DoorBreakRescue> findByEventId(Long eventId);
    boolean existsByEventId(Long eventId);
}
