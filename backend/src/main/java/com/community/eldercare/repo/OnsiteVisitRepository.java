package com.community.eldercare.repo;

import com.community.eldercare.domain.OnsiteVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OnsiteVisitRepository extends JpaRepository<OnsiteVisit, Long> {
    List<OnsiteVisit> findByEventIdOrderByArrivedAtAsc(Long eventId);
}
