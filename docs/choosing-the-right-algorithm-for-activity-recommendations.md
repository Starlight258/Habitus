# Choosing the Right Algorithm for Activity Recommendations

## The Beginning: A Seemingly Simple Problem

I was tasked with building an activity recommendation feature for the Habitus project. The requirement was clear: "
Recommend the optimal combination of activities to users within their available time based on their personal
priorities." At first, I thought it'd be simple—just pick the most efficient activities first, right?

But when I actually started implementing it, things got more complicated than expected.

## First Attempt: Greedy

The first approach that came to mind was Greedy. Select activities with the highest efficiency (score/time) first. It's
simple to implement and runs in O(n log n). Sounds perfect.

I tested it with a simple example: 4 activities, 100 minutes available.

- A: 10min, 11pts (efficiency: 1.1) 👑
- B: 50min, 50pts (efficiency: 1.0)
- C: 50min, 50pts (efficiency: 1.0)
- D: 95min, 99pts (efficiency: 1.04)

Sorted by efficiency: A → D → B → C

1. Select A (10min, 11pts) → 90min remaining
2. Try D → needs 95min ❌
3. Select B (50min, 50pts) → 40min remaining
4. Try C → needs 50min ❌

**Result: A + B = 60min, 61pts**

But when I calculated by hand, **B + C = 100min, 100pts**. Greedy failed.

By selecting A (efficiency 1.1), I couldn't fit D and also missed C. I got caught up in a small efficiency difference
and missed the bigger picture. Greedy only sees "the best right now" and can't guarantee the global optimum.

## Second Thought: Brute Force

What if I try all combinations? With 4 activities, that's 2^4 = 16 cases.

1. {} → 0pts
2. {A} → 11pts
   ...
8. {B,C} → 100pts ✅
9. {A,B,C} → 110min (exceeds limit) ❌

It guarantees the correct answer, but with O(2^n), it takes over a second once you hit 25 activities. Not scalable.

## Final Choice: Dynamic Programming

This is a classic 0-1 Knapsack problem. With DP, we can guarantee the optimal solution in O(n × W).

### Core Idea of DP

```
dp[i][w] = maximum value when considering i activities with w minutes used

dp[i][w] = max(
    dp[i-1][w],                      // don't select
    dp[i-1][w-time[i]] + value[i]    // select
)
```

Building the table:

```
         w=0   w=10  w=50  w=60  w=95  w=100
    ┌────┬─────┬─────┬─────┬─────┬─────┬──────┐
  0 │  0 │  0  │  0  │  0  │  0  │  0  │   0  │
  A │  0 │ 11  │ 11  │ 11  │ 11  │ 11  │  11  │
  B │  0 │ 11  │ 50  │ 61  │ 61  │ 61  │  61  │
  C │  0 │ 11  │ 50  │ 61  │ 61  │ 61  │ 100  │
  D │  0 │ 11  │ 50  │ 61  │ 99  │ 99  │ 100  │
    └────┴─────┴─────┴─────┴─────┴─────┴──────┘
```

dp[4][100] = 100 is our maximum value.

### Backtracking to Find Selected Activities

If the value changed, that activity was selected.

```
dp[4][100] = 100, dp[3][100] = 100 → D not selected
dp[3][100] = 100, dp[2][100] = 61  → C selected! (50min left)
dp[2][50] = 50, dp[1][50] = 11     → B selected! (0min left)
```

**Selected: B, C → 100min, 100pts ✅**

## Implementation

```java
private int[][] buildDpTable(List<SelectedActivity> activities, int maxMinutes) {
    int n = activities.size();
    int[][] dp = new int[n + 1][maxMinutes + 1];

    for (int i = 1; i <= n; i++) {
        SelectedActivity cur = activities.get(i - 1);
        int duration = cur.getActivity().getDurationMinutes();
        int value = cur.getValue();

        for (int w = 0; w <= maxMinutes; w++) {
            dp[i][w] = dp[i - 1][w];
            if (w >= duration) {
                dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - duration] + value);
            }
        }
    }
    return dp;
}
```

Backtracking:

```java
private List<SelectedActivity> backtrack(
        List<SelectedActivity> activities, int[][] dp, int maxMinutes
) {
    List<SelectedActivity> selected = new ArrayList<>();
    int n = activities.size();
    int w = maxMinutes;

    for (int i = n; i > 0 && w > 0; i--) {
        if (dp[i][w] != dp[i - 1][w]) {
            selected.add(activities.get(i - 1));
            w -= activities.get(i - 1).getActivity().getDurationMinutes();
        }
    }

    Collections.reverse(selected);
    return selected;
}
```

## Performance Comparison

<img width="1724" height="906" alt="image" src="https://github.com/user-attachments/assets/1c383b59-745a-4395-81f0-cbcaa599a96d" />

<img width="1572" height="962" alt="image" src="https://github.com/user-attachments/assets/8de11645-c2cb-44e8-9695-b0d4f36c7d7b" />

Current project scale: 5 activities, 10,080 minutes (1 week)

- DP: 5 × 10,080 = 50,400 operations (0.017ms)
- Brute Force: 2^5 = 32 operations

If scaled to 20 activities:

- DP: 20 × 10,080 = 201,600 operations (under 1ms)
- Brute Force: 2^20 = 1,048,576 operations (1 second)

| Algorithm   | Time Complexity | Optimal | Notes                  |
|-------------|-----------------|---------|------------------------|
| Greedy      | O(n log n)      | ❌       | Fast but inaccurate    |
| Brute Force | O(2^n)          | ✅       | Accurate but slow      |
| DP          | O(n × W)        | ✅       | Accurate and efficient |

## Future Improvements

**Recalculation Issue**: The entire DP table recalculates whenever users modify activities. This could be improved with
caching or incremental computation.

**Scaling Up**: For 100+ activities, an approximation algorithm (Greedy + post-processing) could be considered. It
achieves 90-98% accuracy while being 60x faster.

## Conclusion

I initially thought "just pick the most efficient ones first," but Greedy couldn't guarantee the optimal solution. DP
provided both accuracy and efficiency. I learned that choosing an algorithm isn't about "which is fastest" but "which
fits the problem characteristics and input size."

# Korean version - 활동 추천 알고리즘, 선택 기준

## 개요

Habitus 프로젝트에서 활동 추천 기능을 만들게 됐다.
"사용자에게 제한된 시간 안에서 개인 우선순위에 맞는 최적의 활동 조합을 추천하자."
처음엔 효율 좋은 것부터 넣으면 되지 않을까 싶었다. 하지만 막상 구현하려니 생각보다 복잡했다.

## 첫 번째 시도: Greedy

가장 먼저 떠오른 건 Greedy였다. 효율(점수/시간)이 높은 것부터 선택하는 방식이다. 구현도 간단하고 O(n log n)으로 빠르다.

간단한 예시로 테스트했다.

활동 4개, 시간 100분

- A: 10분, 11점 (효율 1.1) 👑
- B: 50분, 50점 (효율 1.0)
- C: 50분, 50점 (효율 1.0)
- D: 95분, 99점 (효율 1.04)

효율 순으로 정렬하면 A → D → B → C다.

1. A 선택 (10분, 11점) → 남은 90분
2. D 시도 → 95분 필요 ❌
3. B 선택 (50분, 50점) → 남은 40분
4. C 시도 → 50분 필요 ❌

**결과: A + B = 60분, 61점**

그런데 손으로 최적화된 조합을 계산하니 **B + C = 100분, 100점**이 나왔다. Greedy가 틀렸다 !

A의 효율(1.1)이 가장 높아서 선택했는데, 그 때문에 D를 못 넣고 C도 놓쳤다.
작은 효율 차이에 집착하다가 큰 그림을 놓친 거다. Greedy는 "지금 당장 최선"만 보기 때문에 전체 최적을 보장하지 못한다. (근시안적인 해를 제공한다)

## 두 번째 고민: Brute Force

모든 경우를 다 해보면? 4개 활동이면 2^4 = 16가지다.

1. {} → 0점
2. {A} → 11점
3. ...
4. {B,C} → 100점 ✅
5. {A,B,C} → 110분 초과 ❌

무조건 정답을 찾지만, O(2^n)이라 활동 25개부터 1초를 넘긴다. 확장성이 없다.

## 최종 선택: Dynamic Programming

이 문제는 전형적인 **0-1 Knapsack**이다. DP로 접근하면 **O(n × W)**로 최적해를 보장받는다.

### DP의 핵심

`dp[i][w]` = i개 활동을 보고, w분 사용했을 때 최대 가치

```
dp[i][w] = max(
    dp[i-1][w],                      // 선택 안함
    dp[i-1][w-time[i]] + value[i]    // 선택함
)
```

테이블을 채우면:

```
         w=0   w=10  w=50  w=60  w=95  w=100
    ┌────┬─────┬─────┬─────┬─────┬─────┬──────┐
  0 │  0 │  0  │  0  │  0  │  0  │  0  │   0  │
  A │  0 │ 11  │ 11  │ 11  │ 11  │ 11  │  11  │
  B │  0 │ 11  │ 50  │ 61  │ 61  │ 61  │  61  │
  C │  0 │ 11  │ 50  │ 61  │ 61  │ 61  │ 100  │
  D │  0 │ 11  │ 50  │ 61  │ 99  │ 99  │ 100  │
    └────┴─────┴─────┴─────┴─────┴─────┴──────┘
```

`dp[4][100] = 100`이 최대 가치다.

### 역추적(backtracking)으로 활동 찾기

값이 다르면 해당 활동을 선택한 것이다.

1. `dp[4][100] = 100, dp[3][100] = 100` → D 선택 안함
2. `dp[3][100] = 100, dp[2][100] = 61` → C 선택! (남은 50분)
3. `dp[2][50] = 50, dp[1][50] = 11` → B 선택! (남은 0분)

**선택: B, C → 100분, 100점 ✅**

## 실제 구현

```java
private int[][] buildDpTable(List<SelectedActivity> activities, int maxMinutes) {
    int n = activities.size();
    int[][] dp = new int[n + 1][maxMinutes + 1];

    for (int i = 1; i <= n; i++) {
        SelectedActivity cur = activities.get(i - 1);
        int duration = cur.getActivity().getDurationMinutes();
        int value = cur.getValue();

        for (int w = 0; w <= maxMinutes; w++) {
            dp[i][w] = dp[i - 1][w];
            if (w >= duration) {
                dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - duration] + value);
            }
        }
    }
    return dp;
}
```

역추적:

```java
private List<SelectedActivity> backtrack(
        List<SelectedActivity> activities, int[][] dp, int maxMinutes
) {
    List<SelectedActivity> selected = new ArrayList<>();
    int n = activities.size();
    int w = maxMinutes;

    for (int i = n; i > 0 && w > 0; i--) {
        if (dp[i][w] != dp[i - 1][w]) {
            selected.add(activities.get(i - 1));
            w -= activities.get(i - 1).getActivity().getDurationMinutes();
        }
    }

    Collections.reverse(selected);
    return selected;
}
```

## 성능 비교

<img width="1724" height="906" alt="image" src="https://github.com/user-attachments/assets/1c383b59-745a-4395-81f0-cbcaa599a96d" />

<img width="1572" height="962" alt="image" src="https://github.com/user-attachments/assets/8de11645-c2cb-44e8-9695-b0d4f36c7d7b" />

현재 프로젝트: 활동 5개, 시간 10,080분(1주일)

- **DP**: 5 × 10,080 = 50,400번 연산 (0.017ms)
- **Brute Force**: 2^5 = 32번 연산

활동 20개로 늘어나면?

- **DP**: 20 × 10,080 = 201,600번 (1ms 이하)
- **Brute Force**: 2^20 = 1,048,576번 (1초)

| 알고리즘        | 시간 복잡도     | 최적해 | 특징       |
|-------------|------------|-----|----------|
| Greedy      | O(n log n) | ❌   | 빠르지만 부정확 |
| Brute Force | O(2^n)     | ✅   | 정확하지만 느림 |
| DP          | O(n × W)   | ✅   | 정확하고 효율적 |

## 향후 개선 방향

**매번 재계산 문제**: 사용자가 활동을 수정할 때마다 전체 재계산한다. 캐싱이나 증분 계산으로 개선 가능하다.

**규모 확장 시**: 활동 100개 이상이면 근사 알고리즘(Greedy + 후처리)을 고려할 수 있다. 90~98% 정확도로 60배 빠르다.

## 마무리하며

처음엔 "효율 높은 것부터 넣으면 되지 않나?"라고 생각했지만, Greedy는 최적해를 보장하지 못했다.
DP는 정확성과 효율성을 모두 보장한다. 알고리즘 선택은 시간 복잡도, 공간 복잡도도 중요하지만 문제 특성에 맞게 적절한 정도의 올바른 해를 보장하는가가 중요하다는 것을 배웠다.
