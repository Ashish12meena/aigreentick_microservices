package com.aigreentick.services.common.service.base.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.aigreentick.services.common.service.BaseService;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Generic base service for JPA entities.
 *
 * @param <T>  Entity type
 * @param <ID> Entity ID type
 */
public abstract class JpaBaseService<T, ID> implements BaseService<T, ID> {

    protected abstract JpaRepository<T, ID> getRepository();

    @Transactional(readOnly = true)
    public T findById(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found with id " + id));

    }

    @Transactional(readOnly = true)
    public Optional<T> findOptionalById(ID id) {
        return getRepository().findById(id);

    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        return getRepository().findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return getRepository().existsById(id);
    }

    @Transactional
    public T save(T entity) {
        return getRepository().save(entity);
    }

    @Transactional
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    @Transactional
    public void delete(T entity) {
        getRepository().delete(entity);
    }

    @Transactional
    public List<T> saveAll(List<T> entities) {
        return getRepository().saveAll(entities);
    }
}
