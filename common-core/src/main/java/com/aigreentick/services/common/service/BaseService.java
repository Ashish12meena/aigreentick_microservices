package com.aigreentick.services.common.service;

import java.util.List;
import java.util.Optional;

public interface BaseService <T,ID> {
    T save(T entity);

    List<T> saveAll(List<T> entities);

    Optional<T> findOptionalById(ID id);

    T findById(ID id);

    boolean existsById(ID id);

    void deleteById(ID id);
}
