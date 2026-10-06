package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorEscalation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoorEscalationRepository extends JpaRepository<DoorEscalation, Long> {
    List<DoorEscalation> findByEvent_IdOrderByStartedAtDesc(Long eventId);

    Optional<DoorEscalation> findFirstByEvent_IdAndStatusOrderByStartedAtDesc(Long eventId, String status);
}
