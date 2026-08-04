package com.suiyou.service.impl;

import com.suiyou.model.Family;
import com.suiyou.model.FamilyMember;
import com.suiyou.repository.FamilyMemberRepository;
import com.suiyou.repository.FamilyRepository;
import com.suiyou.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class FamilyServiceImpl implements FamilyService {

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private FamilyMemberRepository familyMemberRepository;

    @Override
    public Family initFamily(Long creatorId) {
        return createFamily(creatorId, "我的家庭", "CNY", "personal");
    }

    @Override
    public Family createFamily(Long creatorId, String name, String currency) {
        return createFamily(creatorId, name, currency, "FAMILY");
    }

    @Transactional
    private Family createFamily(Long creatorId, String name, String currency, String type) {
        if (familyRepository.findByCreatorId(creatorId) != null) {
            throw new RuntimeException("用户已经创建过家庭");
        }

        Family family = new Family();
        family.setCreatorId(creatorId);
        family.setName(name);
        family.setType(type);
        family.setCurrency(currency);
        Family savedFamily = familyRepository.save(family);

        // 创建家庭成员
        FamilyMember creatorMember = new FamilyMember();
        creatorMember.setFamilyId(savedFamily.getId());
        creatorMember.setUserId(creatorId);
        creatorMember.setRole("OWNER");
        creatorMember.setJoinedAt(LocalDateTime.now());

        familyMemberRepository.save(creatorMember);

        return savedFamily;
    }


    @Override
    public Family getFamilyByUserId(Long userId) {
        return familyRepository.findByCreatorId(userId);
    }

    @Override
    public Family getRequiredFamilyByUserId(Long userId) {
        return familyMemberRepository.findByUserIdAndStatus(userId, 1).stream()
                .findFirst()
                .map(member -> getFamilyById(member.getFamilyId()))
                .orElseThrow(() -> new RuntimeException("用户当前不属于任何家庭"));
    }

    @Override
    public Family getFamilyById(Long familyId) {
        return familyRepository.findByIdAndStatus(familyId, 1);
    }
}
