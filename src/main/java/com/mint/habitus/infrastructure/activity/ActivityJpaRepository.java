package com.mint.habitus.infrastructure.activity;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA Repository
 */
public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, Long> {

    Optional<ActivityEntity> findByName(String name);

    boolean existsByName(String name);
}
