package com.community.eldercare.repo;

import com.community.eldercare.domain.FamilyCommunication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyCommunicationRepository extends JpaRepository<FamilyCommunication, Long> {
    List<FamilyCommunication> findByEventIdOrderByCommunicatedAtAsc(Long eventId);
}
