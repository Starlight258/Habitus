package com.mint.habitus.domain.recommendation.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 시간 제약 조건을 나타내는 Value Object
 */
@Getter
@EqualsAndHashCode
public class TimeConstraint {

    private static final int WEEKLY_AVAILABLE_TIME = 7 * 24 * 60;

    private final int totalMinutes;

    private TimeConstraint(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public static TimeConstraint of(int minutes) {
        validate(minutes);
        return new TimeConstraint(minutes);
    }

    private static void validate(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("시간은 양수여야 합니다: " + minutes);
        }
        if (minutes > WEEKLY_AVAILABLE_TIME) {
            throw new IllegalArgumentException("주간 가용 시간은 1주일(10080분)을 초과할 수 없습니다: " + minutes);
        }
    }

    public boolean canAccommodate(int requiredMinutes) {
        return totalMinutes >= requiredMinutes;
    }

    public TimeConstraint subtract(int minutes) {
        return new TimeConstraint(totalMinutes - minutes);
    }
}
