package com.mint.habitus.domain.activity.domain;

import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.domain.priority.domain.Priority;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 활동 Aggregate Root
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Activity {

    private final Long id;
    private final String name;
    private final String description;
    private final int durationMinutes;

    // TODO: v2에서 사용 예정
    private final int cost;
    private final ActivityEffects effects;

    public static Activity of(
            Long id,
            String name,
            String description,
            int durationMinutes,
            int cost,
            ActivityEffects effects
    ) {
        validateBasicInfo(name, durationMinutes, cost);
        return new Activity(id, name, description, durationMinutes, cost, effects);
    }

    /**
     * 우선순위 기반 활동의 가치 계산
     */
    public int calculateValue(Priority priority) {
        return effects.calculateWeightedValue(priority);
    }

    /**
     * 가용 시간에 따른 활동 수행 가능 여부
     */
    public boolean canBePerformedWithin(int availableMinutes) {
        return durationMinutes <= availableMinutes;
    }

    private static void validateBasicInfo(String name, int durationMinutes, int cost) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("활동명은 필수입니다.");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("활동 시간은 양수여야 합니다: " + durationMinutes);
        }
        if (cost < 0) {
            throw new IllegalArgumentException("비용은 음수일 수 없습니다: " + cost);
        }
    }

    /**
     * 특정 자본에 대한 효과
     */
    public int getEffectOn(CapitalType capitalType) {
        return effects.getEffect(capitalType);
    }

    /**
     * 자본별 원본 효과
     */
    public Map<CapitalType, Integer> getActiveEffects() {
        return effects.getActiveEffects();
    }

    /**
     * 자본별 우선순위 적용된 가중 효과
     */
    public Map<CapitalType, Integer> getWeightedEffects(Priority priority) {
        return effects.getWeightedEffects(priority);
    }
}
