package com.mint.habitus.application.recommendation.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequest {

    private Long userId;
    private Integer availableMinutes;
    private Map<String, Integer> priorities;
}
