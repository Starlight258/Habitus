package com.mint.habitus.domain.optimization.domain;

import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.priority.domain.Priority;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 최적 활동 찾기 Domain Service
 */
@Slf4j
@Component
public class OptimalActivityFinder {

    /**
     * 0-1 Knapsack DP로 최적 활동 조합 도출
     */
    public OptimizationResult find(
            List<Activity> activities,
            Priority priority,
            TimeConstraint timeConstraint
    ) {
        if (activities.isEmpty()) {
            return OptimizationResult.empty(timeConstraint.getTotalMinutes());
        }

        int n = activities.size();
        int W = timeConstraint.getTotalMinutes();

        log.debug("최적화 시작 - 활동: {}개, 가용시간: {}분", n, W);

        // 1. 각 활동의 가치 계산
        List<SelectedActivity> activitiesWithValues = calculateValues(activities, priority);

        // 2. DP 테이블 생성 및 계산
        int[][] dp = buildDpTable(activitiesWithValues, W);

        // 3. 선택된 활동 역추적
        List<SelectedActivity> selected = backtrack(activitiesWithValues, dp, W);

        // 4. 결과 생성
        return buildResult(selected, W);
    }

    /**
     * 각 활동의 가치 계산
     */
    private List<SelectedActivity> calculateValues(List<Activity> activities, Priority priority) {
        return activities.stream()
                .map(activity -> new SelectedActivity(
                        activity,
                        activity.calculateValue(priority)
                ))
                .toList();
    }

    private int[][] buildDpTable(List<SelectedActivity> activities, int maxMinutes) {
        int n = activities.size();
        int[][] dp = new int[n + 1][maxMinutes + 1];

        for (int i = 1; i <= n; i++) {
            SelectedActivity cur = activities.get(i - 1);
            int duration = cur.getActivity().getDurationMinutes();
            int value = cur.getValue();

            for (int w = 0; w <= maxMinutes; w++) {
                // 선택하지 않는 경우
                dp[i][w] = dp[i - 1][w];

                // 선택하는 경우
                if (w >= duration) {
                    int valueIfSelected = dp[i - 1][w - duration] + value;
                    dp[i][w] = Math.max(dp[i][w], valueIfSelected);
                }
            }
        }

        return dp;
    }

    /**
     * 선택된 활동 역추적
     */
    private List<SelectedActivity> backtrack(
            List<SelectedActivity> activities,
            int[][] dp,
            int maxMinutes
    ) {
        List<SelectedActivity> selected = new ArrayList<>();
        int n = activities.size();
        int w = maxMinutes;

        for (int i = n; i > 0 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                SelectedActivity cur = activities.get(i - 1);
                selected.add(cur);
                w -= cur.getActivity().getDurationMinutes();
            }
        }

        Collections.reverse(selected);
        return selected;
    }

    /**
     * 최적화 결과 생성
     */
    private OptimizationResult buildResult(List<SelectedActivity> selected, int availableMinutes) {
        int totalValue = 0;
        int totalMinutes = 0;

        List<SelectedActivity> selectedActivities = new ArrayList<>();

        for (SelectedActivity cur : selected) {
            totalValue += cur.getValue();
            totalMinutes += cur.getActivity().getDurationMinutes();

            selectedActivities.add(SelectedActivity.builder()
                    .activity(cur.getActivity())
                    .value(cur.getValue())
                    .build());
        }

        return OptimizationResult.builder()
                .selectedActivities(selectedActivities)
                .totalValue(totalValue)
                .totalMinutes(totalMinutes)
                .remainingMinutes(availableMinutes - totalMinutes)
                .build();
    }
}
