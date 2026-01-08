package com.mint.habitus.application.recommendation;

import com.mint.habitus.application.recommendation.dto.RecommendationRequest;
import com.mint.habitus.application.recommendation.dto.RecommendationResponse;
import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.activity.domain.ActivityRepository;
import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.domain.priority.domain.Priority;
import com.mint.habitus.domain.priority.domain.PriorityLevel;
import com.mint.habitus.application.recommendation.dto.RecommendationResponse.RecommendedActivity;
import com.mint.habitus.domain.recommendation.domain.OptimalActivityFinder;
import com.mint.habitus.domain.recommendation.domain.RecommendationResult;
import com.mint.habitus.domain.recommendation.domain.SelectedActivity;
import com.mint.habitus.domain.recommendation.domain.TimeConstraint;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주간 최적 활동 조합을 추천하는 Application Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityRecommendationService {

    private final ActivityRepository activityRepository;
    private final OptimalActivityFinder optimalActivityFinder;

    public RecommendationResponse recommendWeeklyActivities(RecommendationRequest request) {
        log.info("최적화된 활동 추천 요청 - userId: {}, 가용시간: {}분", request.getUserId(), request.getAvailableMinutes());

        // 1. 도메인 객체 생성
        Priority priority = createPriority(request.getPriorities());
        TimeConstraint timeConstraint = TimeConstraint.of(request.getAvailableMinutes());

        // 2. 활동 목록 조회
        List<Activity> activities = activityRepository.findAll();

        // 3. 최적화 실행
        RecommendationResult result = optimalActivityFinder.find(activities, priority, timeConstraint);

        // 4. DTO 변환
        return toResponse(result, priority);
    }

    private Priority createPriority(Map<String, Integer> priorityMap) {
        if (priorityMap == null || priorityMap.isEmpty()) {
            return Priority.defaultPriority();
        }

        Map<CapitalType, PriorityLevel> priorities = new EnumMap<>(CapitalType.class);

        priorityMap.forEach((key, weight) -> {
            try {
                CapitalType type = CapitalType.valueOf(key);
                PriorityLevel level = PriorityLevel.fromWeight(weight);
                priorities.put(type, level);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 우선순위 입력: {} = {}", key, weight);
            }
        });

        // 누락된 자본은 기본값(LOW) 설정
        for (CapitalType type : CapitalType.values()) {
            priorities.putIfAbsent(type, PriorityLevel.LOW);
        }

        return Priority.of(priorities);
    }

    private RecommendationResponse toResponse(RecommendationResult result, Priority priority) {
        List<RecommendedActivity> activities = new ArrayList<>();

        for (SelectedActivity selected : result.getSelectedActivities()) {
            Activity activity = selected.getActivity();

            // 원본 효과
            Map<String, Integer> originalEffects = new LinkedHashMap<>();
            activity.getActiveEffects().forEach((type, effect) ->
                    originalEffects.put(type.name(), effect)
            );

            // 가중 효과
            Map<String, Integer> weightedEffects = new LinkedHashMap<>();
            activity.getWeightedEffects(priority).forEach((type, effect) ->
                    weightedEffects.put(type.name(), effect)
            );

            activities.add(RecommendedActivity.builder()
                    .id(activity.getId())
                    .name(activity.getName())
                    .duration(activity.getDurationMinutes())
                    .calculatedValue(selected.getValue())
                    .originalEffects(originalEffects)
                    .weightedEffects(weightedEffects)
                    .build());
        }

        // 자본별 총 증가량
        Map<String, Integer> totalCapitalGain = new LinkedHashMap<>();
        result.getTotalCapitalGains().forEach((type, gain) ->
                totalCapitalGain.put(type.name(), gain)
        );

        return RecommendationResponse.builder()
                .totalValue(result.getTotalValue())
                .totalMinutes(result.getTotalMinutes())
                .remainingMinutes(result.getRemainingMinutes())
                .activityCount(result.getActivityCount())
                .timeUtilizationRate(result.getTimeUtilizationRate())
                .totalCapitalGain(totalCapitalGain)
                .selectedActivities(activities)
                .build();
    }
}
