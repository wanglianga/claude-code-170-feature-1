package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorContactAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoorContactAttemptRepository extends JpaRepository<DoorContactAttempt, Long> {
    List<DoorContactAttempt> findByEventIdOrderBySeqAscIdAsc(Long eventId);
    long countByEventId(Long eventId);
}
