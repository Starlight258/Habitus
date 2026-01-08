package com.mint.habitus.presentation.recommendation;

import com.mint.habitus.application.recommendation.ActivityRecommendationService;
import com.mint.habitus.application.recommendation.dto.RecommendationRequest;
import com.mint.habitus.application.recommendation.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/activities/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final ActivityRecommendationService activityRecommendationService;

    @PostMapping
    public ResponseEntity<RecommendationResponse> optimizeWeekly(
            @RequestBody RecommendationRequest request
    ) {
        log.info("POST /api/activities/recommendation - userId: {}", request.getUserId());

        RecommendationResponse response = activityRecommendationService.recommendWeeklyActivities(request);

        log.info("최적화 완료 - 총 가치: {}, 활동 수: {}",
                response.getTotalValue(), response.getActivityCount());

        return ResponseEntity.ok(response);
    }
}
