package com.mint.habitus.application.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mint.habitus.application.activity.dto.ActivityResponse;
import com.mint.habitus.application.activity.dto.CreateActivityRequest;
import com.mint.habitus.application.activity.dto.UpdateActivityRequest;
import com.mint.habitus.domain.activity.domain.Activity;
import com.mint.habitus.domain.activity.domain.ActivityRepository;
import com.mint.habitus.domain.capital.domain.CapitalType;
import com.mint.habitus.fixture.TestFixture;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityService activityService;

    @Test
    void createActivitySavesValidatedActivity() {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("  Morning run  ")
                .description("Easy pace")
                .durationMinutes(30)
                .cost(null)
                .effects(Map.of("PHYSICAL", 8, "MENTAL", 2))
                .build();

        Activity saved = TestFixture.createActivity(1L, "Morning run", 30, Map.of(
                CapitalType.PHYSICAL, 8,
                CapitalType.MENTAL, 2
        ));
        when(activityRepository.existsByName("Morning run")).thenReturn(false);
        when(activityRepository.save(any(Activity.class))).thenReturn(saved);

        ActivityResponse response = activityService.createActivity(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Morning run");
        assertThat(response.getCost()).isZero();
        assertThat(response.getEffects()).containsEntry("PHYSICAL", 8);
    }

    @Test
    void createActivityRejectsDuplicateNameBeforeSave() {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name("Reading")
                .durationMinutes(60)
                .build();

        when(activityRepository.existsByName("Reading")).thenReturn(true);

        assertThatThrownBy(() -> activityService.createActivity(request))
                .isInstanceOf(DuplicateActivityNameException.class)
                .hasMessage("Activity name already exists");

        verify(activityRepository, never()).save(any());
    }

    @Test
    void createActivityRejectsInvalidInputBeforeDomainLayer() {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .name(" ")
                .durationMinutes(0)
                .cost(-1)
                .effects(Map.of("PHYSICAL", 101, "UNKNOWN", 1))
                .build();

        assertThatThrownBy(() -> activityService.createActivity(request))
                .isInstanceOf(ActivityValidationException.class)
                .hasMessageContaining("name must not be blank")
                .hasMessageContaining("durationMinutes must be between 1 and 1440")
                .hasMessageContaining("cost must be greater than or equal to 0")
                .hasMessageContaining("effects.PHYSICAL must be between 0 and 100")
                .hasMessageContaining("effects.UNKNOWN is not a valid capital type");

        verify(activityRepository, never()).save(any());
    }

    @Test
    void createActivityRejectsMissingRequiredFields() {
        CreateActivityRequest request = CreateActivityRequest.builder()
                .description("Missing name and duration")
                .build();

        assertThatThrownBy(() -> activityService.createActivity(request))
                .isInstanceOf(ActivityValidationException.class)
                .hasMessageContaining("name is required")
                .hasMessageContaining("durationMinutes is required");

        verify(activityRepository, never()).save(any());
    }

    @Test
    void updateActivityAppliesOnlyProvidedFields() {
        Activity current = TestFixture.createActivity(1L, "Reading", 60, Map.of(
                CapitalType.KNOWLEDGE, 5,
                CapitalType.MENTAL, 1
        ));
        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .durationMinutes(45)
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(current));
        when(activityRepository.findByName("Reading")).thenReturn(Optional.of(current));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        activityService.updateActivity(1L, request);

        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());
        Activity updated = captor.getValue();

        assertThat(updated.getName()).isEqualTo("Reading");
        assertThat(updated.getDurationMinutes()).isEqualTo(45);
        assertThat(updated.getEffectOn(CapitalType.KNOWLEDGE)).isEqualTo(5);
        assertThat(updated.getEffectOn(CapitalType.MENTAL)).isEqualTo(1);
    }

    @Test
    void updateActivityRejectsNameConflict() {
        Activity current = TestFixture.createActivity(1L, "Reading", 60, Map.of(CapitalType.KNOWLEDGE, 5));
        Activity duplicate = TestFixture.createActivity(2L, "Running", 30, Map.of(CapitalType.PHYSICAL, 5));
        UpdateActivityRequest request = UpdateActivityRequest.builder()
                .name("Running")
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(current));
        when(activityRepository.findByName("Running")).thenReturn(Optional.of(duplicate));

        assertThatThrownBy(() -> activityService.updateActivity(1L, request))
                .isInstanceOf(DuplicateActivityNameException.class);

        verify(activityRepository, never()).save(any());
    }

    @Test
    void deleteActivityRejectsMissingId() {
        when(activityRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> activityService.deleteActivity(99L))
                .isInstanceOf(ActivityNotFoundException.class)
                .hasMessage("Activity not found");

        verify(activityRepository, never()).delete(any());
    }
}
