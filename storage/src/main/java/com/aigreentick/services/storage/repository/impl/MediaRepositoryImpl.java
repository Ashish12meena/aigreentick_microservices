package com.aigreentick.services.storage.repository.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.storage.model.Media;
import com.aigreentick.services.storage.repository.MediaRepositoryCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;


@Repository
public class MediaRepositoryImpl implements MediaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public int softDeleteById(Long mediaId, Long deletedBy) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Media> update = cb.createCriteriaUpdate(Media.class);
        Root<Media> root = update.from(Media.class);

        update.set(root.get("deleted"), true);
        update.set(root.get("deletedAt"), LocalDateTime.now());
        update.set(root.get("updatedByUserId"), deletedBy);
        update.where(cb.equal(root.get("id"), mediaId));

        return entityManager.createQuery(update).executeUpdate();
    }
    
}
