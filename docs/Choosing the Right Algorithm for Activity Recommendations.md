# Choosing the Right Algorithm for Activity Recommendations

## The Beginning: A Seemingly Simple Problem

I was tasked with building an activity recommendation feature for the Habitus project. The requirement was clear: "Recommend the optimal combination of activities to users within their available time based on their personal priorities." At first, I thought it'd be simple—just pick the most efficient activities first, right?

But when I actually started implementing it, things got more complicated than expected.

## First Attempt: Greedy

The first approach that came to mind was Greedy. Select activities with the highest efficiency (score/time) first. It's simple to implement and runs in O(n log n). Sounds perfect.

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

By selecting A (efficiency 1.1), I couldn't fit D and also missed C. I got caught up in a small efficiency difference and missed the bigger picture. Greedy only sees "the best right now" and can't guarantee the global optimum.

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

Current project scale: 5 activities, 10,080 minutes (1 week)

- DP: 5 × 10,080 = 50,400 operations (0.017ms)
- Brute Force: 2^5 = 32 operations

If scaled to 20 activities:

- DP: 20 × 10,080 = 201,600 operations (under 1ms)
- Brute Force: 2^20 = 1,048,576 operations (1 second)

| Algorithm | Time Complexity | Optimal | Notes |
|-----------|----------------|---------|-------|
| Greedy | O(n log n) | ❌ | Fast but inaccurate |
| Brute Force | O(2^n) | ✅ | Accurate but slow |
| DP | O(n × W) | ✅ | Accurate and efficient |

## Future Improvements

**Recalculation Issue**: The entire DP table recalculates whenever users modify activities. This could be improved with caching or incremental computation.

**Scaling Up**: For 100+ activities, an approximation algorithm (Greedy + post-processing) could be considered. It achieves 90-98% accuracy while being 60x faster.

## Conclusion

I initially thought "just pick the most efficient ones first," but Greedy couldn't guarantee the optimal solution. DP provided both accuracy and efficiency. I learned that choosing an algorithm isn't about "which is fastest" but "which fits the problem characteristics and input size."
