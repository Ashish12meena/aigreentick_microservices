package com.aigreentick.services.shared.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.shared.enums.blacklist.BlacklistType;
import com.aigreentick.services.shared.model.Blacklist;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    @Query("""
                SELECT b FROM Blacklist b
                WHERE b.mobile IN :mobiles
                AND b.countryId = :countryId
                AND b.isBlocked = true
                AND b.deletedAt IS NULL
                AND (b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP)
                AND (
                    b.type = 'BLOCKED_BY_ADMIN' OR
                    (b.type = 'BLOCKED_BY_USER' AND b.senderId = :senderId)
                )
            """)
    List<Blacklist> findBlacklistedNumbers(List<String> mobiles, Long countryId, Long senderId);

    @Query("""
            SELECT b FROM Blacklist b
            WHERE b.mobile IN :mobiles
            AND b.countryId = :countryId
            AND b.isBlocked = true
            AND b.deletedAt IS NULL
            AND (b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP)
            AND b.type = 'BLOCKED_BY_ADMIN'
            """)
    List<Blacklist> findBlacklistNumbersBlockedByAdmin(List<String> mobiles, Long countryId);

    @Query("""
            SELECT b FROM Blacklist b
            WHERE b.mobile IN :mobiles
            AND b.countryId = :countryId
            AND b.isBlocked = true
            AND b.deletedAt IS NULL
            AND (b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP)
            AND b.type = 'BLOCKED_BY_USER'
            AND b.senderId = :senderId
            """)
    List<Blacklist> findBlacklistNumbersBlockedByUser(List<String> mobiles, Long countryId, Long senderId);

    @Query("""
                SELECT b FROM Blacklist b
                WHERE (:mobile IS NULL OR b.mobile LIKE %:mobile%)
                AND (:countryId IS NULL OR b.countryId = :countryId)
                AND (:type IS NULL OR b.type = :type)
                AND (:isBlocked IS NULL OR b.isBlocked = :isBlocked)
                AND (:isExpired IS NULL OR (
                    CASE
                        WHEN :isExpired = TRUE THEN b.expiresAt IS NOT NULL AND b.expiresAt < CURRENT_TIMESTAMP
                        ELSE b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP
                    END
                ))
                AND b.deletedAt IS NULL
            """)
    Page<Blacklist> findFilteredBlacklistEntries(
            @Param("mobile") String mobile,
            @Param("countryId") Long countryId,
            @Param("type") BlacklistType type,
            @Param("isBlocked") Boolean isBlocked,
            @Param("isExpired") Boolean isExpired,
            Pageable pageable);

    @Query("""
                SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
                FROM Blacklist b
                WHERE b.mobile = :mobile
                  AND b.type = :type
                  AND b.senderId = :senderId
                  AND b.isBlocked = true
                  AND (b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP)
                  AND b.deletedAt IS NULL
            """)
    boolean existsByUser(String mobile, BlacklistType type, Long senderId);

    @Query("""
                SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
                FROM Blacklist b
                WHERE b.mobile = :mobile
                  AND b.countryId = :countryId
                  AND b.type = :type
                  AND b.senderId IS NULL
                  AND b.isBlocked = true
                  AND (b.expiresAt IS NULL OR b.expiresAt > CURRENT_TIMESTAMP)
                  AND b.deletedAt IS NULL
            """)
    boolean existsByAdmin(String mobile, Long countryId, BlacklistType type);

    
}


