package com.mint.habitus.infrastructure.activity;

import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.activity.domain.ActivityEffects;
import com.mint.habitus.domain.capital.domain.CapitalType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public Activity toDomain(ActivityEntity entity) {
        Map<CapitalType, Integer> effects = new EnumMap<>(CapitalType.class);
        effects.put(CapitalType.PHYSICAL, entity.getPhysicalEffect());
        effects.put(CapitalType.MENTAL, entity.getMentalEffect());
        effects.put(CapitalType.KNOWLEDGE, entity.getKnowledgeEffect());
        effects.put(CapitalType.CULTURAL, entity.getCulturalEffect());
        effects.put(CapitalType.LINGUISTIC, entity.getLinguisticEffect());
        effects.put(CapitalType.SOCIAL, entity.getSocialEffect());
        effects.put(CapitalType.ECONOMIC, entity.getEconomicEffect());

        return Activity.of(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDurationMinutes(),
                entity.getCost(),
                ActivityEffects.of(effects)
        );
    }

    public ActivityEntity toEntity(Activity domain) {
        return ActivityEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .durationMinutes(domain.getDurationMinutes())
                .cost(domain.getCost())
                .physicalEffect(domain.getEffectOn(CapitalType.PHYSICAL))
                .mentalEffect(domain.getEffectOn(CapitalType.MENTAL))
                .knowledgeEffect(domain.getEffectOn(CapitalType.KNOWLEDGE))
                .culturalEffect(domain.getEffectOn(CapitalType.CULTURAL))
                .linguisticEffect(domain.getEffectOn(CapitalType.LINGUISTIC))
                .socialEffect(domain.getEffectOn(CapitalType.SOCIAL))
                .economicEffect(domain.getEffectOn(CapitalType.ECONOMIC))
                .build();
    }
}
