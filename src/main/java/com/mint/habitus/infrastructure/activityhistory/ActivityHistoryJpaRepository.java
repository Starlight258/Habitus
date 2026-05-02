package com.mint.habitus.infrastructure.activityhistory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityHistoryJpaRepository extends JpaRepository<ActivityHistoryEntity, Long> {
}
