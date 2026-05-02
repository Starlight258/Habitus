package com.mint.habitus.infrastructure.activityhistory;

import com.mint.habitus.domain.activityhistory.domain.ActivityHistory;
import com.mint.habitus.domain.activityhistory.domain.ActivityHistoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ActivityHistoryRepositoryImpl implements ActivityHistoryRepository {

    private final ActivityHistoryJpaRepository jpaRepository;
    private final ActivityHistoryMapper mapper;

    @Override
    public List<ActivityHistory> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ActivityHistory> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public ActivityHistory save(ActivityHistory activityHistory) {
        ActivityHistoryEntity entity = mapper.toEntity(activityHistory);
        ActivityHistoryEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
