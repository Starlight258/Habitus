package com.mint.habitus.application.activity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityListResponse {

    private List<ActivityResponse> activities;
    private Integer total;

    public static ActivityListResponse of(List<ActivityResponse> activities) {
        return ActivityListResponse.builder()
                .activities(activities)
                .total(activities.size())
                .build();
    }
}
