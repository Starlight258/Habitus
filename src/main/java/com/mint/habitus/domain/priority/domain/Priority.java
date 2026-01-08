package com.mint.habitus.domain.priority.domain;

import com.mint.habitus.domain.capital.domain.CapitalType;
import java.util.EnumMap;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * 사용자의 자본 우선순위를 나타내는 Value Object
 */
@EqualsAndHashCode
public class Priority {

    private final Map<CapitalType, PriorityLevel> priorities;

    private Priority(Map<CapitalType, PriorityLevel> priorities) {
        this.priorities = new EnumMap<>(priorities);
    }

    public static Priority of(Map<CapitalType, PriorityLevel> priorities) {
        validatePriorities(priorities);
        return new Priority(priorities);
    }

    public static Priority defaultPriority() {
        Map<CapitalType, PriorityLevel> defaults = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            defaults.put(type, PriorityLevel.LOW);
        }
        return new Priority(defaults);
    }

    private static void validatePriorities(Map<CapitalType, PriorityLevel> priorities) {
        if (priorities == null || priorities.isEmpty()) {
            throw new IllegalArgumentException("우선순위는 비어있을 수 없습니다.");
        }
    }

    public int getWeight(CapitalType type) {
        return priorities.getOrDefault(type, PriorityLevel.LOW).getWeight();
    }

    public PriorityLevel getLevel(CapitalType type) {
        return priorities.getOrDefault(type, PriorityLevel.LOW);
    }
}
