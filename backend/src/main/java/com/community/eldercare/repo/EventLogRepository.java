package com.community.eldercare.repo;

import com.community.eldercare.domain.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findByEventIdOrderByCreatedAtAsc(Long eventId);
}
