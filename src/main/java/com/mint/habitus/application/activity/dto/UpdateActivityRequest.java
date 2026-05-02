package com.mint.habitus.application.activity.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateActivityRequest {

    private String name;
    private String description;
    private Integer durationMinutes;
    private Integer cost;
    private Map<String, Integer> effects;
}
