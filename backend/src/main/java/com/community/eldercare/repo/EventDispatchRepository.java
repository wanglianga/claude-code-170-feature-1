package com.community.eldercare.repo;

import com.community.eldercare.domain.EventDispatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventDispatchRepository extends JpaRepository<EventDispatch, Long> {
    List<EventDispatch> findByEventIdOrderByNotifiedAtAsc(Long eventId);
}
