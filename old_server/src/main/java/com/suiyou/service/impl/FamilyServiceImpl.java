package com.suiyou.service.impl;

import com.suiyou.model.Family;
import com.suiyou.repository.FamilyRepository;
import com.suiyou.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Family getFamilyByUserId(Long userId) {
        return familyRepository.findByCreatorId(userId);
    }

    @Override
    @Transactional
    public Family ensureFamily(Long creatorId) {
        Family family = familyRepository.findByCreatorId(creatorId);
        if (family != null) return family;
        return createFamily(creatorId, "我的家庭", "CNY");
    }

    @Override
    public Family getFamilyById(Long familyId) {
        return familyRepository.findByIdAndStatus(familyId, 1);
    }
}
