package com.community.eldercare.repo;

import com.community.eldercare.domain.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByElderId(Long elderId);
    void deleteByElderId(Long elderId);
}
