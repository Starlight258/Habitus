package com.mint.habitus.domain.activity.domain;

import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.domain.priority.domain.Priority;
import java.util.EnumMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 활동이 각 자본에 미치는 효과를 나타내는 Value Object
 */
@Getter
@EqualsAndHashCode
public class ActivityEffects {

    private final Map<CapitalType, Integer> effects;

    private ActivityEffects(Map<CapitalType, Integer> effects) {
        this.effects = new EnumMap<>(effects);
    }

    public static ActivityEffects of(Map<CapitalType, Integer> effects) {
        validateEffects(effects);
        return new ActivityEffects(effects);
    }

    public static ActivityEffects empty() {
        Map<CapitalType, Integer> empty = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            empty.put(type, 0);
        }
        return new ActivityEffects(empty);
    }

    private static void validateEffects(Map<CapitalType, Integer> effects) {
        if (effects == null) {
            throw new IllegalArgumentException("효과는 null일 수 없습니다.");
        }

        effects.values().forEach(value -> {
            if (value < 0) {
                throw new IllegalArgumentException("효과 점수는 음수일 수 없습니다: " + value);
            }
        });
    }

    /**
     * 각 자본별 우선순위를 반영한 가중치 적용 가치 계산
     * 가치 = 활동에 따른 효과 점수 * 우선순위 점수
     */
    public int calculateWeightedValue(Priority priority) {
        return effects.entrySet().stream()
                .mapToInt(entry -> entry.getValue() * priority.getWeight(entry.getKey()))
                .sum();
    }

    public int getEffect(CapitalType type) {
        return effects.getOrDefault(type, 0);
    }

    /**
     * 원본 효과
     */
    public Map<CapitalType, Integer> getActiveEffects() {
        Map<CapitalType, Integer> active = new EnumMap<>(CapitalType.class);
        effects.forEach((type, value) -> {
            if (value > 0) {
                active.put(type, value);
            }
        });
        return active;
    }

    /**
     * 우선순위 곱한 효과
     */
    public Map<CapitalType, Integer> getWeightedEffects(Priority priority) {
        Map<CapitalType, Integer> weighted = new EnumMap<>(CapitalType.class);
        effects.forEach((type, effect) -> {
            if (effect > 0) {
                weighted.put(type, effect * priority.getWeight(type));
            }
        });
        return weighted;
    }
}
