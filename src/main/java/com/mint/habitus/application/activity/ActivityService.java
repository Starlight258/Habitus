package com.mint.habitus.application.activity;

import com.mint.habitus.application.activity.dto.ActivityListResponse;
import com.mint.habitus.application.activity.dto.ActivityResponse;
import com.mint.habitus.application.activity.dto.CreateActivityRequest;
import com.mint.habitus.application.activity.dto.UpdateActivityRequest;
import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.activity.domain.ActivityEffects;
import com.mint.habitus.domain.activity.domain.ActivityRepository;
import com.mint.habitus.domain.capital.domain.CapitalType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_DURATION_MINUTES = 1440;
    private static final int MAX_EFFECT_VALUE = 100;

    private final ActivityRepository activityRepository;

    public ActivityListResponse getActivities() {
        List<ActivityResponse> activities = activityRepository.findAll().stream()
                .map(ActivityResponse::from)
                .toList();

        return ActivityListResponse.of(activities);
    }

    public ActivityResponse getActivity(Long id) {
        return ActivityResponse.from(findActivity(id));
    }

    @Transactional
    public ActivityResponse createActivity(CreateActivityRequest request) {
        ValidatedActivityPayload payload = validateCreateRequest(request);

        if (activityRepository.existsByName(payload.name())) {
            throw new DuplicateActivityNameException(payload.name());
        }

        Activity activity = Activity.of(
                null,
                payload.name(),
                payload.description(),
                payload.durationMinutes(),
                payload.cost(),
                ActivityEffects.of(payload.effects())
        );

        return ActivityResponse.from(activityRepository.save(activity));
    }

    @Transactional
    public ActivityResponse updateActivity(Long id, UpdateActivityRequest request) {
        Activity current = findActivity(id);
        ValidatedActivityPayload payload = validateUpdateRequest(request, current);

        activityRepository.findByName(payload.name())
                .filter(activity -> !activity.getId().equals(id))
                .ifPresent(activity -> {
                    throw new DuplicateActivityNameException(payload.name());
                });

        Activity updated = Activity.of(
                current.getId(),
                payload.name(),
                payload.description(),
                payload.durationMinutes(),
                payload.cost(),
                ActivityEffects.of(payload.effects())
        );

        return ActivityResponse.from(activityRepository.save(updated));
    }

    @Transactional
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ActivityNotFoundException(id);
        }

        activityRepository.delete(id);
    }

    private Activity findActivity(Long id) {
        if (id == null) {
            throw new ActivityNotFoundException(null);
        }

        return activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException(id));
    }

    private ValidatedActivityPayload validateCreateRequest(CreateActivityRequest request) {
        if (request == null) {
            throw new ActivityValidationException("Validation failed: request body is required");
        }

        List<String> errors = new ArrayList<>();
        String name = validateName(request.getName(), true, errors);
        String description = validateDescription(request.getDescription(), errors);
        Integer durationMinutes = validateDuration(request.getDurationMinutes(), true, errors);
        Integer cost = validateCost(request.getCost(), errors);
        Map<CapitalType, Integer> effects = validateEffects(request.getEffects(), errors);

        throwIfInvalid(errors);

        return new ValidatedActivityPayload(name, description, durationMinutes, cost == null ? 0 : cost, effects);
    }

    private ValidatedActivityPayload validateUpdateRequest(UpdateActivityRequest request, Activity current) {
        if (request == null) {
            throw new ActivityValidationException("Validation failed: request body is required");
        }

        List<String> errors = new ArrayList<>();
        String name = request.getName() == null
                ? current.getName()
                : validateName(request.getName(), false, errors);
        String description = request.getDescription() == null
                ? current.getDescription()
                : validateDescription(request.getDescription(), errors);
        Integer durationMinutes = request.getDurationMinutes() == null
                ? current.getDurationMinutes()
                : validateDuration(request.getDurationMinutes(), false, errors);
        Integer cost = request.getCost() == null
                ? current.getCost()
                : validateCost(request.getCost(), errors);
        Map<CapitalType, Integer> effects = request.getEffects() == null
                ? currentEffects(current)
                : validateEffects(request.getEffects(), errors);

        throwIfInvalid(errors);

        return new ValidatedActivityPayload(name, description, durationMinutes, cost, effects);
    }

    private String validateName(String name, boolean required, List<String> errors) {
        if (name == null) {
            if (required) {
                errors.add("name is required");
            }
            return null;
        }

        String trimmed = name.trim();
        if (trimmed.isBlank()) {
            errors.add("name must not be blank");
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            errors.add("name must be at most 100 characters");
        }

        return trimmed;
    }

    private String validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.add("description must be at most 500 characters");
        }

        return description;
    }

    private Integer validateDuration(Integer durationMinutes, boolean required, List<String> errors) {
        if (durationMinutes == null) {
            if (required) {
                errors.add("durationMinutes is required");
            }
            return null;
        }
        if (durationMinutes < 1 || durationMinutes > MAX_DURATION_MINUTES) {
            errors.add("durationMinutes must be between 1 and 1440");
        }

        return durationMinutes;
    }

    private Integer validateCost(Integer cost, List<String> errors) {
        if (cost != null && cost < 0) {
            errors.add("cost must be greater than or equal to 0");
        }

        return cost;
    }

    private Map<CapitalType, Integer> validateEffects(Map<String, Integer> requestEffects, List<String> errors) {
        Map<CapitalType, Integer> effects = emptyEffects();
        if (requestEffects == null) {
            return effects;
        }

        requestEffects.forEach((key, value) -> {
            CapitalType type = parseCapitalType(key, errors);
            if (value == null) {
                errors.add("effects." + key + " is required");
            } else if (value < 0 || value > MAX_EFFECT_VALUE) {
                errors.add("effects." + key + " must be between 0 and 100");
            }

            if (type != null && value != null && value >= 0 && value <= MAX_EFFECT_VALUE) {
                effects.put(type, value);
            }
        });

        return effects;
    }

    private CapitalType parseCapitalType(String key, List<String> errors) {
        if (key == null || key.isBlank()) {
            errors.add("effects key must be a valid capital type");
            return null;
        }

        try {
            return CapitalType.valueOf(key);
        } catch (IllegalArgumentException e) {
            errors.add("effects." + key + " is not a valid capital type");
            return null;
        }
    }

    private Map<CapitalType, Integer> emptyEffects() {
        Map<CapitalType, Integer> effects = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            effects.put(type, 0);
        }
        return effects;
    }

    private Map<CapitalType, Integer> currentEffects(Activity activity) {
        Map<CapitalType, Integer> effects = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            effects.put(type, activity.getEffectOn(type));
        }
        return effects;
    }

    private void throwIfInvalid(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ActivityValidationException("Validation failed: " + String.join(", ", errors));
        }
    }

    private record ValidatedActivityPayload(
            String name,
            String description,
            Integer durationMinutes,
            Integer cost,
            Map<CapitalType, Integer> effects
    ) {
    }
}
