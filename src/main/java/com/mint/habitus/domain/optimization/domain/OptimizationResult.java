package com.mint.habitus.domain.optimization.domain;

import com.mint.habitus.domain.capital.domain.CapitalType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * 최적화 결과를 나타내는 Value Object
 */
@Getter
@Builder
public class OptimizationResult {

    private final List<SelectedActivity> selectedActivities;
    private final int totalValue;
    private final int totalMinutes;
    private final int remainingMinutes;

    public static OptimizationResult empty(int availableMinutes) {
        return OptimizationResult.builder()
                .selectedActivities(Collections.emptyList())
                .totalValue(0)
                .totalMinutes(0)
                .remainingMinutes(availableMinutes)
                .build();
    }

    /**
     * 자본별 총 증가량 계산
     */
    public Map<CapitalType, Integer> getTotalCapitalGains() {
        Map<CapitalType, Integer> gains = new EnumMap<>(CapitalType.class);

        for (SelectedActivity selected : selectedActivities) {
            selected.getActivity().getActiveEffects().forEach((type, effect) ->
                    gains.merge(type, effect, Integer::sum)
            );
        }

        return gains;
    }

    /**
     * 선택된 활동 개수
     */
    public int getActivityCount() {
        return selectedActivities.size();
    }

    /**
     * 시간 사용률 (%)
     */
    public double getTimeUtilizationRate() {
        int totalAvailable = totalMinutes + remainingMinutes;
        return totalAvailable > 0 ? (double) totalMinutes / totalAvailable * 100 : 0;
    }
}
