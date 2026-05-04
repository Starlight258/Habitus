package com.mint.habitus.application.activityhistory.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityHistoryListResponse {

    private List<ActivityHistoryResponse> histories;

    public static ActivityHistoryListResponse of(List<ActivityHistoryResponse> histories) {
        return ActivityHistoryListResponse.builder()
                .histories(histories)
                .build();
    }
}
