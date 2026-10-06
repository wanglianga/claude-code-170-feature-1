package com.community.eldercare.repo;

import com.community.eldercare.domain.EventFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventFollowUpRepository extends JpaRepository<EventFollowUp, Long> {
    List<EventFollowUp> findByEventIdOrderByFollowedUpAtAsc(Long eventId);
}
