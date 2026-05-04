package com.mint.habitus.application.activityhistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mint.habitus.application.activityhistory.dto.ActivityHistoryResponse;
import com.mint.habitus.application.activityhistory.dto.CreateActivityHistoryRequest;
import com.mint.habitus.application.activityhistory.exception.ActivityHistoryNotFoundException;
import com.mint.habitus.application.activityhistory.exception.ActivityHistoryValidationException;
import com.mint.habitus.domain.activity.domain.ActivityRepository;
import com.mint.habitus.domain.activityhistory.domain.ActivityHistory;
import com.mint.habitus.domain.activityhistory.domain.ActivityHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityHistoryServiceTest {

    @Mock
    private ActivityHistoryRepository activityHistoryRepository;

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityHistoryService activityHistoryService;

    @Test
    void createHistorySavesValidatedHistory() {
        LocalDateTime performedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        CreateActivityHistoryRequest request = CreateActivityHistoryRequest.builder()
                .activityId(1L)
                .performedAt(performedAt)
                .durationMinutes(30)
                .notes("felt great")
                .build();
        ActivityHistory saved = ActivityHistory.of(10L, 1L, performedAt, 30, "felt great");

        when(activityRepository.existsById(1L)).thenReturn(true);
        when(activityHistoryRepository.save(any(ActivityHistory.class))).thenReturn(saved);

        ActivityHistoryResponse response = activityHistoryService.createHistory(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getActivityId()).isEqualTo(1L);
        assertThat(response.getPerformedAt()).isEqualTo(performedAt);
        assertThat(response.getDurationMinutes()).isEqualTo(30);
        assertThat(response.getNotes()).isEqualTo("felt great");
    }

    @Test
    void createHistoryDefaultsPerformedAtWhenMissing() {
        CreateActivityHistoryRequest request = CreateActivityHistoryRequest.builder()
                .activityId(1L)
                .build();

        when(activityRepository.existsById(1L)).thenReturn(true);
        when(activityHistoryRepository.save(any(ActivityHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        activityHistoryService.createHistory(request);

        ArgumentCaptor<ActivityHistory> captor = ArgumentCaptor.forClass(ActivityHistory.class);
        verify(activityHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getPerformedAt()).isNotNull();
    }

    @Test
    void createHistoryRejectsMissingActivityTemplate() {
        CreateActivityHistoryRequest request = CreateActivityHistoryRequest.builder()
                .activityId(99L)
                .durationMinutes(30)
                .build();

        when(activityRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> activityHistoryService.createHistory(request))
                .isInstanceOf(ActivityHistoryValidationException.class)
                .hasMessageContaining("activityId does not reference an activity");

        verify(activityHistoryRepository, never()).save(any());
    }

    @Test
    void createHistoryRejectsInvalidInputBeforeSave() {
        CreateActivityHistoryRequest request = CreateActivityHistoryRequest.builder()
                .durationMinutes(0)
                .notes("a".repeat(501))
                .build();

        assertThatThrownBy(() -> activityHistoryService.createHistory(request))
                .isInstanceOf(ActivityHistoryValidationException.class)
                .hasMessageContaining("activityId is required")
                .hasMessageContaining("durationMinutes must be between 1 and 1440")
                .hasMessageContaining("notes must be at most 500 characters");

        verify(activityRepository, never()).existsById(any());
        verify(activityHistoryRepository, never()).save(any());
    }

    @Test
    void getHistoriesReturnsAllHistories() {
        LocalDateTime performedAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        when(activityHistoryRepository.findAll(0, 20)).thenReturn(List.of(
                ActivityHistory.of(1L, 10L, performedAt, 30, "first"),
                ActivityHistory.of(2L, 11L, performedAt.plusDays(1), null, null)
        ));

        assertThat(activityHistoryService.getHistories(0, 20).getHistories())
                .hasSize(2)
                .extracting(ActivityHistoryResponse::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    void getHistoryRejectsMissingId() {
        when(activityHistoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityHistoryService.getHistory(404L))
                .isInstanceOf(ActivityHistoryNotFoundException.class)
                .hasMessage("Activity history not found: id=404");
    }

    @Test
    void deleteHistoryRejectsMissingId() {
        when(activityHistoryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> activityHistoryService.deleteHistory(404L))
                .isInstanceOf(ActivityHistoryNotFoundException.class)
                .hasMessage("Activity history not found: id=404");

        verify(activityHistoryRepository, never()).delete(any());
    }
}
