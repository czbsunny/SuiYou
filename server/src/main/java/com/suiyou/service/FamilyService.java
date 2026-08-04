package com.suiyou.service;

import com.suiyou.model.Family;

public interface FamilyService {
    Family initFamily(Long creatorId);

    Family createFamily(Long creatorId, String name, String currency);

    Family getFamilyByUserId(Long userId);

    Family getRequiredFamilyByUserId(Long userId);

    Family getFamilyById(Long familyId);
}
