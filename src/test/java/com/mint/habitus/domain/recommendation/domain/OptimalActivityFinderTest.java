package com.mint.habitus.domain.recommendation.domain;

import static com.mint.habitus.fixture.TestFixture.createActivity;
import static com.mint.habitus.fixture.TestFixture.createDefaultPriority;
import static com.mint.habitus.fixture.TestFixture.createPriority;
import static com.mint.habitus.fixture.TestFixture.createPriorityWithHighKnowledge;
import static com.mint.habitus.fixture.TestFixture.createPriorityWithHighPhysical;
import static com.mint.habitus.fixture.TestFixture.createTestActivities;

import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.domain.priority.domain.Priority;
import com.mint.habitus.domain.priority.domain.PriorityLevel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OptimalActivityFinder 도메인 테스트")
class OptimalActivityFinderTest {

    private OptimalActivityFinder finder;
    private List<Activity> activities;

    @BeforeEach
    void setUp() {
        finder = new OptimalActivityFinder();
        activities = createTestActivities();
    }

    @Test
    @DisplayName("기본 최적화: 시간 제약 내 최대 가치 활동 선택")
    void findOptimal_basic() {
        // given
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(90);

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalMinutes()).isLessThanOrEqualTo(90);
            softly.assertThat(result.getRemainingMinutes()).isGreaterThanOrEqualTo(0);
            softly.assertThat(result.getActivityCount()).isGreaterThan(0);
            softly.assertThat(result.getTotalValue()).isGreaterThan(0);
        });
    }

    @Test
    @DisplayName("우선순위 반영: PHYSICAL 우선순위 높으면 운동이 높은 가치")
    void findOptimal_priorityPhysical() {
        // given
        Priority physicalPriority = createPriorityWithHighPhysical();
        TimeConstraint timeConstraint = TimeConstraint.of(100);

        /*
         * PHYSICAL 우선순위 3, 나머지 1:
         * 1. 운동 30분: (4×3) + (1×1) = 13점 ⭐
         * 2. 독서 60분: (5×1) + (2×1) = 7점
         * 3. 명상 20분: (4×3) + (1×1) = 13점 ⭐
         *
         * 최적 조합 (100분):
         * - 운동(30분, 13점) + 명상(20분, 13점) + 독서(60분, 7점) = 110분 (불가)
         * - 운동(30분, 13점) + 독서(60분, 7점) = 90분, 20점 ✅
         * 또는
         * - 명상(20분, 13점) + 독서(60분, 7점) = 80분, 20점
         */

        // when
        RecommendationResult result = finder.find(activities, physicalPriority, timeConstraint);

        // then
        // PHYSICAL 효과가 있는 활동이 선택되어야 함
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .toList();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalMinutes()).isLessThanOrEqualTo(100);

            // 운동 또는 명상 중 최소 하나 포함
            softly.assertThat(selectedNames)
                    .anyMatch(name -> name.equals("운동 30분") || name.equals("명상 20분"));

            softly.assertThat(result.getTotalValue()).isGreaterThanOrEqualTo(20);
        });
    }

    @Test
    @DisplayName("우선순위 반영: KNOWLEDGE 우선순위 높으면 독서 선택")
    void findOptimal_priorityKnowledge() {
        // given
        Priority knowledgePriority = createPriorityWithHighKnowledge();
        TimeConstraint timeConstraint = TimeConstraint.of(100);

        // when
        RecommendationResult result = finder.find(activities, knowledgePriority, timeConstraint);

        // then
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .toList();

        SoftAssertions.assertSoftly(softly ->
                softly.assertThat(selectedNames).contains("독서 60분")
        );
    }

    @Test
    @DisplayName("시간 제약: 가용 시간을 초과하지 않음")
    void findOptimal_timeConstraintRespected() {
        // given
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(50);

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalMinutes()).isLessThanOrEqualTo(50);
            softly.assertThat(result.getTotalMinutes() + result.getRemainingMinutes()).isEqualTo(50);
        });
    }

    @Test
    @DisplayName("최적 조합: 정확히 계산된 최적 활동 조합 반환")
    void findOptimal_multipleActivities() {
        // given
        Priority priority = createDefaultPriority(); // 모든 우선순위 1
        TimeConstraint timeConstraint = TimeConstraint.of(200);

        /*
         * 테스트 활동 목록 (우선순위 모두 1):
         * 1. 운동 30분: (4×1) + (1×1) = 5점
         * 2. 독서 60분: (5×1) + (2×1) = 7점
         * 3. 명상 20분: (4×1) + (1×1) = 5점
         * 4. 커뮤니티 120분: (5×1) + (2×1) = 7점
         * 5. 영어 90분: (5×1) + (3×1) = 8점
         *
         * 최적 조합 (200분 제약):
         * - 독서(60분, 7점) + 영어(90분, 8점) + 명상(20분, 5점) + 운동(30분, 5점)
         * - 총 200분, 총 25점
         */

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        // 선택된 활동 확인
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .sorted()
                .toList();

        // 자본 증가 검증
        Map<CapitalType, Integer> gains = result.getTotalCapitalGains();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalMinutes()).isEqualTo(200); // 200분 모두 사용
            softly.assertThat(result.getRemainingMinutes()).isEqualTo(0);
            softly.assertThat(result.getTotalValue()).isEqualTo(25); // 정확히 25점
            softly.assertThat(result.getActivityCount()).isEqualTo(4); // 4개 활동

            softly.assertThat(selectedNames).containsExactlyInAnyOrder(
                    "운동 30분",
                    "독서 60분",
                    "명상 20분",
                    "영어 학습 90분"
            );

            softly.assertThat(gains.get(CapitalType.PHYSICAL)).isEqualTo(5); // 4 + 1
            softly.assertThat(gains.get(CapitalType.MENTAL)).isEqualTo(5);   // 1 + 4
            softly.assertThat(gains.get(CapitalType.KNOWLEDGE)).isEqualTo(8); // 5 + 3
            softly.assertThat(gains.get(CapitalType.CULTURAL)).isEqualTo(2);
            softly.assertThat(gains.get(CapitalType.LINGUISTIC)).isEqualTo(5); // 5
            softly.assertThat(gains.get(CapitalType.SOCIAL)).isNull();
            softly.assertThat(gains.get(CapitalType.ECONOMIC)).isNull();
        });
    }

    @Test
    @DisplayName("Edge Case: 빈 활동 리스트")
    void findOptimal_emptyActivities() {
        // given
        List<Activity> emptyList = Collections.emptyList();
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(100);

        // when
        RecommendationResult result = finder.find(emptyList, priority, timeConstraint);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getActivityCount()).isZero();
            softly.assertThat(result.getTotalValue()).isZero();
            softly.assertThat(result.getTotalMinutes()).isZero();
            softly.assertThat(result.getRemainingMinutes()).isEqualTo(100);
        });
    }

    @Test
    @DisplayName("Edge Case: 가용 시간 부족 (모든 활동 시간 초과)")
    void findOptimal_insufficientTime() {
        // given
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(10); // 10분만

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getActivityCount()).isZero();
            softly.assertThat(result.getRemainingMinutes()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("가치 계산: 선택된 활동의 총 가치가 일치")
    void findOptimal_valueCalculation() {
        // given
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(150);

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        int expectedValue = result.getSelectedActivities().stream()
                .mapToInt(SelectedActivity::getValue)
                .sum();

        SoftAssertions.assertSoftly(softly ->
                softly.assertThat(result.getTotalValue()).isEqualTo(expectedValue)
        );
    }

    @Test
    @DisplayName("DP 알고리즘 정확성: 간단한 케이스로 최적해 검증")
    void findOptimal_knapsackCorrectness() {
        // given: 간단한 활동 3개
        List<Activity> simpleActivities = List.of(
                createActivity(1L, "A활동 10분", 10,
                        Map.of(CapitalType.PHYSICAL, 6)),
                createActivity(2L, "B활동 20분", 20,
                        Map.of(CapitalType.MENTAL, 10)),
                createActivity(3L, "C활동 30분", 30,
                        Map.of(CapitalType.KNOWLEDGE, 12))
        );

        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(50);

        /*
         * 가능한 조합:
         * 1. A(10분, 6) + B(20분, 10) + C(30분, 12) = 60분 (불가)
         * 2. A(10분, 6) + B(20분, 10) = 30분, 16점
         * 3. A(10분, 6) + C(30분, 12) = 40분, 18점
         * 4. B(20분, 10) + C(30분, 12) = 50분, 22점 ⭐ 최적
         * 5. A만 = 10분, 6점
         * 6. B만 = 20분, 10점
         * 7. C만 = 30분, 12점
         */

        // when
        RecommendationResult result = finder.find(simpleActivities, priority, timeConstraint);

        // then: 정확히 B + C 조합 선택
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .sorted()
                .toList();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalValue()).isEqualTo(22);
            softly.assertThat(result.getTotalMinutes()).isEqualTo(50);
            softly.assertThat(result.getActivityCount()).isEqualTo(2);
            softly.assertThat(selectedNames).containsExactly("B활동 20분", "C활동 30분");
        });
    }

    @Test
    @DisplayName("DP 알고리즘 정확성: 작은 활동 여러개 vs 큰 활동 하나")
    void findOptimal_smallVsLarge() {
        // given
        List<Activity> activities = List.of(
                createActivity(1L, "작은활동1", 20,
                        Map.of(CapitalType.PHYSICAL, 5)),
                createActivity(2L, "작은활동2", 20,
                        Map.of(CapitalType.MENTAL, 5)),
                createActivity(3L, "작은활동3", 20,
                        Map.of(CapitalType.KNOWLEDGE, 5)),
                createActivity(4L, "큰활동", 50,
                        Map.of(CapitalType.CULTURAL, 14))
        );

        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(60);

        /*
         * - 작은활동1 + 작은활동2 + 작은활동3 = 60분, 15점 ⭐ 최적
         * - 큰활동 = 50분, 14점
         * - 큰활동 + 작은활동1 = 70분 (불가)
         */

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then: 작은 활동 3개 선택이 최적
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .sorted()
                .toList();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalValue()).isEqualTo(15);
            softly.assertThat(result.getTotalMinutes()).isEqualTo(60);
            softly.assertThat(result.getActivityCount()).isEqualTo(3);
            softly.assertThat(selectedNames).containsExactly("작은활동1", "작은활동2", "작은활동3");
        });
    }

    @Test
    @DisplayName("DP 알고리즘 적합도 평가: Greedy로는 틀리는 케이스")
    void findOptimal_greedyFails() {
        // given
        List<Activity> activities = List.of(
                createActivity(1L, "고효율", 10,
                        Map.of(CapitalType.PHYSICAL, 7)),    // 효율 0.7
                createActivity(2L, "중효율1", 40,
                        Map.of(CapitalType.MENTAL, 20)),     // 효율 0.5
                createActivity(3L, "중효율2", 40,
                        Map.of(CapitalType.KNOWLEDGE, 20))   // 효율 0.5
        );

        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(80);

        /*
         * Greedy (효율 순): 고효율(10분, 7) + 중효율1(40분, 20) + 중효율2(40분, 20) = 90분 (불가)
         *                  → 고효율(10분, 7) + 중효율1(40분, 20) = 50분, 27점
         *
         * DP (최적): 중효율1(40분, 20) + 중효율2(40분, 20) = 80분, 40점 ⭐
         */

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        List<String> selectedNames = result.getSelectedActivities().stream()
                .map(selected -> selected.getActivity().getName())
                .sorted()
                .toList();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result.getTotalValue()).isEqualTo(40);
            softly.assertThat(result.getTotalMinutes()).isEqualTo(80);
            softly.assertThat(result.getActivityCount()).isEqualTo(2);
            softly.assertThat(selectedNames).containsExactly("중효율1", "중효율2");
            softly.assertThat(selectedNames).doesNotContain("고효율"); // 고효율 제외!
        });
    }

    @Test
    @DisplayName("우선순위 가중치: 높은 우선순위일수록 더 높은 가치")
    void findOptimal_priorityWeight() {
        // given: 같은 활동, 다른 우선순위
        Activity physicalActivity = createActivity(
                1L, "운동 30분", 30,
                Map.of(CapitalType.PHYSICAL, 5)
        );
        List<Activity> singleActivity = List.of(physicalActivity);
        TimeConstraint timeConstraint = TimeConstraint.of(60);

        Priority lowPriority = createPriority(Map.of(CapitalType.PHYSICAL, PriorityLevel.LOW));
        Priority highPriority = createPriority(Map.of(CapitalType.PHYSICAL, PriorityLevel.HIGH));

        // when
        RecommendationResult lowResult = finder.find(singleActivity, lowPriority, timeConstraint);
        RecommendationResult highResult = finder.find(singleActivity, highPriority, timeConstraint);

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(highResult.getTotalValue()).isGreaterThan(lowResult.getTotalValue());

            // LOW: 5 × 1 = 5
            softly.assertThat(lowResult.getTotalValue()).isEqualTo(5);

            // HIGH: 5 × 3 = 15
            softly.assertThat(highResult.getTotalValue()).isEqualTo(15);
        });
    }

    @Test
    @DisplayName("자본 증가: 선택된 활동들의 효과 합산")
    void findOptimal_capitalGains() {
        // given
        Priority priority = createDefaultPriority();
        TimeConstraint timeConstraint = TimeConstraint.of(200);

        // when
        RecommendationResult result = finder.find(activities, priority, timeConstraint);

        // then
        Map<CapitalType, Integer> gains = result.getTotalCapitalGains();

        // 선택된 활동이 있으면 최소 하나의 자본은 증가
        SoftAssertions.assertSoftly(softly -> {
            if (result.getActivityCount() > 0) {
                softly.assertThat(gains.values().stream().mapToInt(Integer::intValue).sum())
                        .isGreaterThan(0);
            }
        });
    }
}
