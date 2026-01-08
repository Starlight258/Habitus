package com.mint.habitus.infrastructure.activity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA Repository
 */
public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, Long> {
}
