package com.mint.habitus.application.activity.dto;

import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.capital.domain.CapitalType;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityResponse {

    private Long id;
    private String name;
    private String description;
    private Integer durationMinutes;
    private Integer cost;
    private Map<String, Integer> effects;

    public static ActivityResponse from(Activity activity) {
        Map<String, Integer> effects = new LinkedHashMap<>();
        for (CapitalType type : CapitalType.values()) {
            effects.put(type.name(), activity.getEffectOn(type));
        }

        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .durationMinutes(activity.getDurationMinutes())
                .cost(activity.getCost())
                .effects(effects)
                .build();
    }
}
