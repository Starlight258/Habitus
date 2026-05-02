package com.mint.habitus.presentation.activity;

import com.mint.habitus.application.activity.ActivityService;
import com.mint.habitus.application.activity.dto.ActivityListResponse;
import com.mint.habitus.application.activity.dto.ActivityResponse;
import com.mint.habitus.application.activity.dto.CreateActivityRequest;
import com.mint.habitus.application.activity.dto.UpdateActivityRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ActivityListResponse> getActivities() {
        return ResponseEntity.ok(activityService.getActivities());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@RequestBody CreateActivityRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ResponseEntity
                .created(URI.create("/api/activities/" + response.getId()))
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable Long id,
            @RequestBody UpdateActivityRequest request
    ) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
