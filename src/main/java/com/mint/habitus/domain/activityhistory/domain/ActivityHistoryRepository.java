package com.mint.habitus.domain.activityhistory.domain;

import java.util.List;
import java.util.Optional;

public interface ActivityHistoryRepository {

    List<ActivityHistory> findAll(int page, int size);

    Optional<ActivityHistory> findById(Long id);

    ActivityHistory save(ActivityHistory activityHistory);

    void delete(Long id);

    boolean existsById(Long id);
}
