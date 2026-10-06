package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorFamilyReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoorFamilyReviewRepository extends JpaRepository<DoorFamilyReview, Long> {
    List<DoorFamilyReview> findByEscalation_IdOrderByCreatedAtAsc(Long escalationId);
}
