package com.mint.habitus.domain.activityhistory.domain;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActivityHistory {

    public static final int MIN_DURATION_MINUTES = 1;
    public static final int MAX_DURATION_MINUTES = 1440;
    public static final int MAX_NOTES_LENGTH = 500;

    private final Long id;
    private final Long activityId;
    private final LocalDateTime performedAt;
    private final Integer durationMinutes;
    private final String notes;

    public static ActivityHistory of(
            Long id,
            Long activityId,
            LocalDateTime performedAt,
            Integer durationMinutes,
            String notes
    ) {
        validate(activityId, durationMinutes, notes);
        return new ActivityHistory(
                id,
                activityId,
                performedAt == null ? LocalDateTime.now() : performedAt,
                durationMinutes,
                notes
        );
    }

    /** Reconstitutes a persisted aggregate without applying creation-time defaults. */
    public static ActivityHistory reconstitute(Long id, Long activityId, LocalDateTime performedAt,
                                               Integer durationMinutes, String notes) {
        return new ActivityHistory(id, activityId, performedAt, durationMinutes, notes);
    }

    private static void validate(Long activityId, Integer durationMinutes, String notes) {
        if (activityId == null) {
            throw new IllegalArgumentException("activityId is required");
        }
        if (durationMinutes != null
                && (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_MINUTES)) {
            throw new IllegalArgumentException("durationMinutes must be between 1 and 1440");
        }
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            throw new IllegalArgumentException("notes must be at most 500 characters");
        }
    }
}
