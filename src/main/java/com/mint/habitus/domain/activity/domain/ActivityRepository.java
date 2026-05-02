package com.mint.habitus.domain.activity.domain;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository {

    List<Activity> findAll();

    Optional<Activity> findById(Long id);

    Optional<Activity> findByName(String name);

    boolean existsById(Long id);

    boolean existsByName(String name);

    Activity save(Activity activity);

    void delete(Long id);
}
