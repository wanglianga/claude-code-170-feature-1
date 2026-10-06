package com.community.eldercare.repo;

import com.community.eldercare.domain.DoorCostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoorCostItemRepository extends JpaRepository<DoorCostItem, Long> {
    List<DoorCostItem> findByForcedEntry_IdOrderByCreatedAtAsc(Long forcedEntryId);
}
