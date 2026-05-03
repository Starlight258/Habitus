package com.mint.habitus.application.activityhistory;

import com.mint.habitus.application.activityhistory.dto.ActivityHistoryListResponse;
import com.mint.habitus.application.activityhistory.dto.ActivityHistoryResponse;
import com.mint.habitus.application.activityhistory.dto.CreateActivityHistoryRequest;
import com.mint.habitus.application.activityhistory.exception.ActivityHistoryNotFoundException;
import com.mint.habitus.application.activityhistory.exception.ActivityHistoryValidationException;
import com.mint.habitus.domain.activity.domain.ActivityRepository;
import com.mint.habitus.domain.activityhistory.domain.ActivityHistory;
import com.mint.habitus.domain.activityhistory.domain.ActivityHistoryRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityHistoryService {

    private final ActivityHistoryRepository activityHistoryRepository;
    private final ActivityRepository activityRepository;

    public ActivityHistoryListResponse getHistories(int page, int size) {
        List<ActivityHistoryResponse> histories = activityHistoryRepository.findAll(page, size).stream()
                .map(ActivityHistoryResponse::from)
                .toList();

        return ActivityHistoryListResponse.of(histories);
    }

    public ActivityHistoryResponse getHistory(Long id) {
        return ActivityHistoryResponse.from(findHistory(id));
    }

    @Transactional
    public ActivityHistoryResponse createHistory(CreateActivityHistoryRequest request) {
        ValidatedActivityHistoryPayload payload = validateCreateRequest(request);

        if (!activityRepository.existsById(payload.activityId())) {
            throw new ActivityHistoryValidationException("Validation failed: activityId does not reference an activity");
        }

        ActivityHistory activityHistory = ActivityHistory.of(
                null,
                payload.activityId(),
                payload.performedAt(),
                payload.durationMinutes(),
                payload.notes()
        );

        return ActivityHistoryResponse.from(activityHistoryRepository.save(activityHistory));
    }

    @Transactional
    public void deleteHistory(Long id) {
        findHistory(id);
        activityHistoryRepository.delete(id);
    }

    private ActivityHistory findHistory(Long id) {
        if (id == null) {
            throw new ActivityHistoryNotFoundException(null);
        }

        return activityHistoryRepository.findById(id)
                .orElseThrow(() -> new ActivityHistoryNotFoundException(id));
    }

    private ValidatedActivityHistoryPayload validateCreateRequest(CreateActivityHistoryRequest request) {
        if (request == null) {
            throw new ActivityHistoryValidationException("Validation failed: request body is required");
        }

        List<String> errors = new ArrayList<>();
        Long activityId = validateActivityId(request.getActivityId(), errors);
        Integer durationMinutes = validateDuration(request.getDurationMinutes(), errors);
        String notes = validateNotes(request.getNotes(), errors);

        throwIfInvalid(errors);

        return new ValidatedActivityHistoryPayload(
                activityId,
                request.getPerformedAt() == null ? LocalDateTime.now() : request.getPerformedAt(),
                durationMinutes,
                notes
        );
    }

    private Long validateActivityId(Long activityId, List<String> errors) {
        if (activityId == null) {
            errors.add("activityId is required");
        }

        return activityId;
    }

    private Integer validateDuration(Integer durationMinutes, List<String> errors) {
        if (durationMinutes != null
                && (durationMinutes < ActivityHistory.MIN_DURATION_MINUTES
                        || durationMinutes > ActivityHistory.MAX_DURATION_MINUTES)) {
            errors.add("durationMinutes must be between 1 and 1440");
        }

        return durationMinutes;
    }

    private String validateNotes(String notes, List<String> errors) {
        if (notes != null && notes.length() > ActivityHistory.MAX_NOTES_LENGTH) {
            errors.add("notes must be at most 500 characters");
        }

        return notes;
    }

    private void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ActivityHistoryValidationException("Validation failed: " + String.join(", ", errors));
        }
    }

    private record ValidatedActivityHistoryPayload(
            Long activityId,
            LocalDateTime performedAt,
            Integer durationMinutes,
            String notes
    ) {
    }
}
