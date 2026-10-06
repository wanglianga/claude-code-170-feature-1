package com.community.eldercare.repo;

import com.community.eldercare.domain.EventService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventServiceRepository extends JpaRepository<EventService, Long> {
    List<EventService> findByEventIdOrderByServiceAtAsc(Long eventId);
}
