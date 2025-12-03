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
    public Family createFamily(Long creatorId, String name) {
        Family family = new Family();
        family.setCreatorId(creatorId);
        family.setName(name);
        return familyRepository.save(family);
    }
}
