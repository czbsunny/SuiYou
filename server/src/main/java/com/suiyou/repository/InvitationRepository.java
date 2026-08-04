package com.suiyou.repository;

import com.suiyou.model.Invitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationTypeAndCredential(String invitationType, String credential);

    Optional<Invitation> findByFamilyIdAndInvitationTypeAndAcceptedAtIsNullAndExpiresAtAfter(
            Long familyId, String invitationType, java.time.LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.invitationType = :invitationType and i.credential = :credential")
    Optional<Invitation> findForUpdate(@Param("invitationType") String invitationType,
                                       @Param("credential") String credential);
}
