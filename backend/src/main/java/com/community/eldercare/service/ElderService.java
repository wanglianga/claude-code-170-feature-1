package com.community.eldercare.service;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.domain.Elder;
import com.community.eldercare.domain.EmergencyContact;
import com.community.eldercare.domain.NeighborResponder;
import com.community.eldercare.repo.ElderRepository;
import com.community.eldercare.repo.EmergencyContactRepository;
import com.community.eldercare.repo.NeighborResponderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElderService {

    private final ElderRepository elderRepo;
    private final EmergencyContactRepository contactRepo;
    private final NeighborResponderRepository neighborRepo;

    public ElderService(ElderRepository elderRepo, EmergencyContactRepository contactRepo,
                        NeighborResponderRepository neighborRepo) {
        this.elderRepo = elderRepo;
        this.contactRepo = contactRepo;
        this.neighborRepo = neighborRepo;
    }

    @Transactional
    public Elder save(Elder elder) {
        if (elder.getId() == null) {
            elder.setCreatedAt(LocalDateTime.now());
        }
        elder.setUpdatedAt(LocalDateTime.now());
        return elderRepo.save(elder);
    }

    @Transactional
    public EmergencyContact saveContact(Long elderId, EmergencyContact c) {
        Elder elder = elderRepo.findById(elderId).orElseThrow(() -> new ApiException("老人不存在"));
        c.setElder(elder);
        return contactRepo.save(c);
    }

    @Transactional
    public NeighborResponder saveNeighbor(Long elderId, NeighborResponder n) {
        Elder elder = elderRepo.findById(elderId).orElseThrow(() -> new ApiException("老人不存在"));
        n.setElder(elder);
        return neighborRepo.save(n);
    }

    @Transactional(readOnly = true)
    public List<EmergencyContact> contacts(Long elderId) {
        return contactRepo.findByElderId(elderId);
    }

    @Transactional(readOnly = true)
    public List<NeighborResponder> neighbors(Long elderId) {
        return neighborRepo.findByElderId(elderId);
    }

    public void deleteContact(Long id) {
        contactRepo.deleteById(id);
    }

    public void deleteNeighbor(Long id) {
        neighborRepo.deleteById(id);
    }
}
