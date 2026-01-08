package com.mint.habitus.domain.priority.domain;

import lombok.Getter;

@Getter
public enum PriorityLevel {

    LOW(1, "낮음"),
    MEDIUM(2, "중간"),
    HIGH(3, "높음");

    private final int weight;
    private final String description;

    PriorityLevel(int weight, String description) {
        this.weight = weight;
        this.description = description;
    }

    public static PriorityLevel fromWeight(int weight) {
        return switch (weight) {
            case 1 -> LOW;
            case 2 -> MEDIUM;
            case 3 -> HIGH;
            default -> throw new IllegalArgumentException("우선순위는 1~3 사이여야 합니다: " + weight);
        };
    }
}
