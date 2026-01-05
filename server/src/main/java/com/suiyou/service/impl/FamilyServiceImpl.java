package com.suiyou.service.impl;

import com.suiyou.model.Family;
import com.suiyou.repository.FamilyRepository;
import com.suiyou.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FamilyServiceImpl implements FamilyService {

    @Autowired
    private FamilyRepository familyRepository;

    @Override
    @Transactional
    public Family createFamily(Long creatorId, String name, String currency) {
        Family family = new Family();
        family.setCreatorId(creatorId);
        family.setName(name);
        if (currency != null && !currency.isEmpty()) {
            family.setCurrency(currency);
        }
        return familyRepository.save(family);
    }

    @Override
    public List<Family> getFamiliesByUserId(Long userId) {
        return familyRepository.findByCreatorIdAndStatus(userId, 1);
    }

    @Override
    public Family getFirstActiveFamilyByUserId(Long userId) {
        List<Family> families = familyRepository.findByCreatorIdAndStatus(userId, 1);
        if (families != null && !families.isEmpty()) {
            return families.get(0);
        }
        return null;
    }

    @Override
    public Family getFamilyById(Long familyId) {
        return familyRepository.findByIdAndStatus(familyId, 1);
    }
}
