package com.mint.habitus.domain.activity.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Activity JPA Entity
 */
@Entity
@Table(name = "activity_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer cost = 0;

    @Column(name = "physical_effect", nullable = false)
    private Integer physicalEffect = 0;

    @Column(name = "mental_effect", nullable = false)
    private Integer mentalEffect = 0;

    @Column(name = "knowledge_effect", nullable = false)
    private Integer knowledgeEffect = 0;

    @Column(name = "cultural_effect", nullable = false)
    private Integer culturalEffect = 0;

    @Column(name = "linguistic_effect", nullable = false)
    private Integer linguisticEffect = 0;

    @Column(name = "social_effect", nullable = false)
    private Integer socialEffect = 0;

    @Column(name = "economic_effect", nullable = false)
    private Integer economicEffect = 0;
}
