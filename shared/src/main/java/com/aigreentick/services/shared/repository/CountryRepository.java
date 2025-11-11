package com.aigreentick.services.shared.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aigreentick.services.shared.model.Country;

@Repository
public interface CountryRepository extends JpaRepository<Country,Long>   {
    Optional<Country> findByName(String name);

    Optional<Country> findByMobileCodeAndDeletedAtIsNull(String code);

    Optional<Country> findByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Optional<Long> findIdByMobileCodeAndDeletedAtIsNull(String mobileCode);

    boolean existsByIsoCode(String isoCode);

    boolean existsByIsoCodeAndIsDeletedIsFalse(String isoCode);

    boolean existsByNameIgnoreCaseAndIsDeletedIsFalse(String name);

    Optional<Long> findIdByMobileCodeAndIsDeletedIsFalse(String mobileCode);

    Optional<Country> findByMobileCodeAndIsDeletedIsFalse(String code);

    Optional<Country> findByNameIgnoreCaseAndIsDeletedIsFalse(String name);


}
