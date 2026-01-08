package com.mint.habitus.application.recommendation.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendedActivity {
        private Long id;
        private String name;
        private Integer duration;
        private Integer calculatedValue;
        private Map<String, Integer> originalEffects;
        private Map<String, Integer> weightedEffects;
    }

    private Integer totalValue;
    private Integer totalMinutes;
    private Integer remainingMinutes;
    private Integer activityCount;
    private Double timeUtilizationRate;
    private Map<String, Integer> totalCapitalGain;
    private List<RecommendedActivity> selectedActivities;
}
