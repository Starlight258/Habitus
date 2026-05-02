package com.mint.habitus.infrastructure.activityhistory;

import com.mint.habitus.domain.activityhistory.domain.ActivityHistory;
import org.springframework.stereotype.Component;

@Component
public class ActivityHistoryMapper {

    public ActivityHistory toDomain(ActivityHistoryEntity entity) {
        return ActivityHistory.of(
                entity.getId(),
                entity.getActivityId(),
                entity.getPerformedAt(),
                entity.getDurationMinutes(),
                entity.getNotes()
        );
    }

    public ActivityHistoryEntity toEntity(ActivityHistory domain) {
        return ActivityHistoryEntity.builder()
                .id(domain.getId())
                .activityId(domain.getActivityId())
                .performedAt(domain.getPerformedAt())
                .durationMinutes(domain.getDurationMinutes())
                .notes(domain.getNotes())
                .build();
    }
}
