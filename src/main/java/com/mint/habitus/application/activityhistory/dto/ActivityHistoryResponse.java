package com.mint.habitus.application.activityhistory.dto;

import com.mint.habitus.domain.activityhistory.domain.ActivityHistory;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityHistoryResponse {

    private Long id;
    private Long activityId;
    private LocalDateTime performedAt;
    private Integer durationMinutes;
    private String notes;

    public static ActivityHistoryResponse from(ActivityHistory activityHistory) {
        return ActivityHistoryResponse.builder()
                .id(activityHistory.getId())
                .activityId(activityHistory.getActivityId())
                .performedAt(activityHistory.getPerformedAt())
                .durationMinutes(activityHistory.getDurationMinutes())
                .notes(activityHistory.getNotes())
                .build();
    }
}
