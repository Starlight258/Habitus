package com.mint.habitus.presentation.activityhistory;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mint.habitus.infrastructure.activity.ActivityEntity;
import com.mint.habitus.infrastructure.activity.ActivityJpaRepository;
import com.mint.habitus.infrastructure.activityhistory.ActivityHistoryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActivityHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityJpaRepository activityJpaRepository;

    @Autowired
    private ActivityHistoryJpaRepository activityHistoryJpaRepository;

    private Long activityId;

    @BeforeEach
    void setUp() {
        activityHistoryJpaRepository.deleteAll();
        activityJpaRepository.deleteAll();
        activityId = activityJpaRepository.save(ActivityEntity.builder()
                .name("Morning run")
                .description("Created in test")
                .durationMinutes(30)
                .cost(0)
                .physicalEffect(10)
                .mentalEffect(3)
                .knowledgeEffect(0)
                .culturalEffect(0)
                .linguisticEffect(0)
                .socialEffect(0)
                .economicEffect(0)
                .build()).getId();
    }

    @Test
    void getHistoriesReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/activity-histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.histories", hasSize(0)));
    }

    @Test
    void createAndGetHistory() throws Exception {
        String historyId = createHistory();

        mockMvc.perform(get("/api/activity-histories/{id}", historyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Integer.parseInt(historyId)))
                .andExpect(jsonPath("$.activityId").value(activityId.intValue()))
                .andExpect(jsonPath("$.performedAt").value("2024-01-15T10:30:00"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.notes").value("felt great"));
    }

    @Test
    void createHistoryReturnsCreatedWithLocationHeader() throws Exception {
        mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": %d,
                                  "performedAt": "2024-01-15T10:30:00",
                                  "durationMinutes": 30,
                                  "notes": "felt great"
                                }
                                """.formatted(activityId)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/activity-histories/\\d+")))
                .andExpect(jsonPath("$.activityId").value(activityId.intValue()))
                .andExpect(jsonPath("$.performedAt").value("2024-01-15T10:30:00"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.notes").value("felt great"));
    }

    @Test
    void createHistoryDefaultsPerformedAt() throws Exception {
        mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": %d
                                }
                                """.formatted(activityId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activityId").value(activityId.intValue()))
                .andExpect(jsonPath("$.performedAt").exists());
    }

    @Test
    void getHistoriesReturnsCreatedHistories() throws Exception {
        createHistory();

        mockMvc.perform(get("/api/activity-histories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.histories", hasSize(1)))
                .andExpect(jsonPath("$.histories[0].activityId").value(activityId.intValue()));
    }

    @Test
    void createHistoryForMissingActivityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": 99999,
                                  "durationMinutes": 30
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "activityId does not reference an activity")));
    }

    @Test
    void createInvalidHistoryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "durationMinutes": 0,
                                  "notes": "%s"
                                }
                                """.formatted("a".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("activityId is required")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "durationMinutes must be between 1 and 1440")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "notes must be at most 500 characters")));
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is invalid"));
    }

    @Test
    void getMissingHistoryReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/activity-histories/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACTIVITY_HISTORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Activity history not found"));
    }

    @Test
    void deleteHistoryRemovesHistory() throws Exception {
        String historyId = createHistory();

        mockMvc.perform(delete("/api/activity-histories/{id}", historyId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activity-histories/{id}", historyId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMissingHistoryReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/activity-histories/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Activity history not found"));
    }

    private String createHistory() throws Exception {
        String location = mockMvc.perform(post("/api/activity-histories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "activityId": %d,
                                  "performedAt": "2024-01-15T10:30:00",
                                  "durationMinutes": 30,
                                  "notes": "felt great"
                                }
                                """.formatted(activityId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return location.substring(location.lastIndexOf('/') + 1);
    }
}
