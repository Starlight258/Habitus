package com.mint.habitus.fixture;

import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.activity.domain.ActivityEffects;
import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.domain.priority.domain.Priority;
import com.mint.habitus.domain.priority.domain.PriorityLevel;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class TestFixture {

    public static List<Activity> createTestActivities() {
        return List.of(
                createActivity(1L, "운동 30분", 30,
                        Map.of(CapitalType.PHYSICAL, 4, CapitalType.MENTAL, 1)),

                createActivity(2L, "독서 60분", 60,
                        Map.of(CapitalType.KNOWLEDGE, 5, CapitalType.CULTURAL, 2)),

                createActivity(3L, "명상 20분", 20,
                        Map.of(CapitalType.MENTAL, 4, CapitalType.PHYSICAL, 1)),

                createActivity(4L, "커뮤니티 120분", 120,
                        Map.of(CapitalType.SOCIAL, 5, CapitalType.LINGUISTIC, 2)),

                createActivity(5L, "영어 학습 90분", 90,
                        Map.of(CapitalType.LINGUISTIC, 5, CapitalType.KNOWLEDGE, 3))
        );
    }

    public static Activity createActivity(Long id, String name, int duration, Map<CapitalType, Integer> effectsMap) {
        Map<CapitalType, Integer> fullEffects = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            fullEffects.put(type, effectsMap.getOrDefault(type, 0));
        }

        return Activity.of(
                id,
                name,
                "테스트 활동",
                duration,
                0,
                ActivityEffects.of(fullEffects)
        );
    }

    public static Priority createDefaultPriority() {
        Map<CapitalType, PriorityLevel> priorities = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            priorities.put(type, PriorityLevel.LOW);
        }
        return Priority.of(priorities);
    }

    public static Priority createPriorityWithHighPhysical() {
        Map<CapitalType, PriorityLevel> priorities = new EnumMap<>(CapitalType.class);
        priorities.put(CapitalType.PHYSICAL, PriorityLevel.HIGH);
        for (CapitalType type : CapitalType.values()) {
            if (type != CapitalType.PHYSICAL) {
                priorities.put(type, PriorityLevel.LOW);
            }
        }
        return Priority.of(priorities);
    }

    public static Priority createPriorityWithHighKnowledge() {
        Map<CapitalType, PriorityLevel> priorities = new EnumMap<>(CapitalType.class);
        priorities.put(CapitalType.KNOWLEDGE, PriorityLevel.HIGH);
        for (CapitalType type : CapitalType.values()) {
            if (type != CapitalType.KNOWLEDGE) {
                priorities.put(type, PriorityLevel.LOW);
            }
        }
        return Priority.of(priorities);
    }

    public static Priority createPriority(Map<CapitalType, PriorityLevel> specificPriorities) {
        Map<CapitalType, PriorityLevel> priorities = new EnumMap<>(CapitalType.class);
        for (CapitalType type : CapitalType.values()) {
            priorities.put(type, specificPriorities.getOrDefault(type, PriorityLevel.LOW));
        }
        return Priority.of(priorities);
    }
}
