package com.community.eldercare.repo;

import com.community.eldercare.domain.NeighborResponder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeighborResponderRepository extends JpaRepository<NeighborResponder, Long> {
    List<NeighborResponder> findByElderId(Long elderId);
    void deleteByElderId(Long elderId);
}
