package com.mint.habitus.presentation.activityhistory;

import com.mint.habitus.application.activityhistory.ActivityHistoryService;
import com.mint.habitus.application.activityhistory.dto.ActivityHistoryListResponse;
import com.mint.habitus.application.activityhistory.dto.ActivityHistoryResponse;
import com.mint.habitus.application.activityhistory.dto.CreateActivityHistoryRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/activity-histories")
@RequiredArgsConstructor
public class ActivityHistoryController {

    private final ActivityHistoryService activityHistoryService;

    @GetMapping
    public ResponseEntity<ActivityHistoryListResponse> getHistories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(activityHistoryService.getHistories(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityHistoryResponse> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(activityHistoryService.getHistory(id));
    }

    @PostMapping
    public ResponseEntity<ActivityHistoryResponse> createHistory(@RequestBody CreateActivityHistoryRequest request) {
        ActivityHistoryResponse response = activityHistoryService.createHistory(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long id) {
        activityHistoryService.deleteHistory(id);
        return ResponseEntity.noContent().build();
    }
}
