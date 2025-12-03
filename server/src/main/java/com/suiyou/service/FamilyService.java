package com.suiyou.service;

import com.suiyou.model.Family;
import org.springframework.stereotype.Service;

@Service
public interface FamilyService {
    Family createFamily(Long creatorId, String name);
}
