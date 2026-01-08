package com.mint.habitus.domain.activity.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA Repository
 */
public interface ActivityJpaRepository extends JpaRepository<ActivityEntity, Long> {
}
