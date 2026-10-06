package com.community.eldercare.repo;

import com.community.eldercare.domain.ForcedEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForcedEntryRepository extends JpaRepository<ForcedEntry, Long> {
    Optional<ForcedEntry> findByEscalation_Id(Long escalationId);
}
