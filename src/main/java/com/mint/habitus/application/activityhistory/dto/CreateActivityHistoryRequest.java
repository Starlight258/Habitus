package com.mint.habitus.application.activityhistory.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateActivityHistoryRequest {

    private Long activityId;
    private LocalDateTime performedAt;
    private Integer durationMinutes;
    private String notes;
}
