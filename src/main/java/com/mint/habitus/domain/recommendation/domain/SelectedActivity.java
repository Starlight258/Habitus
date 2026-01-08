package com.mint.habitus.domain.recommendation.domain;

import com.mint.habitus.domain.activity.domain.Activity;
import lombok.Builder;
import lombok.Getter;

/**
 * 활동별 가치 합계 Value Object
 */
@Builder
@Getter
public final class SelectedActivity {

    private final Activity activity;
    private final int value;

    public SelectedActivity(Activity activity, int value) {
        this.activity = activity;
        this.value = value;
    }
}
