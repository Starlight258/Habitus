package com.mint.habitus.presentation.activity;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mint.habitus.infrastructure.activity.ActivityJpaRepository;
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
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityJpaRepository activityJpaRepository;

    @BeforeEach
    void setUp() {
        activityJpaRepository.deleteAll();
    }

    @Test
    void getActivitiesReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activities", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void createAndGetActivity() throws Exception {
        String createdId = createActivity("Morning run", 30);

        mockMvc.perform(get("/api/activities/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Integer.parseInt(createdId)))
                .andExpect(jsonPath("$.name").value("Morning run"))
                .andExpect(jsonPath("$.durationMinutes").value(30))
                .andExpect(jsonPath("$.cost").value(0))
                .andExpect(jsonPath("$.effects.PHYSICAL").value(10))
                .andExpect(jsonPath("$.effects.MENTAL").value(3));
    }

    @Test
    void createActivityReturnsCreatedWithLocationHeader() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Reading",
                                  "description": "Books",
                                  "durationMinutes": 60,
                                  "cost": 5,
                                  "effects": {"KNOWLEDGE": 9}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/activities/\\d+")))
                .andExpect(jsonPath("$.name").value("Reading"))
                .andExpect(jsonPath("$.effects.KNOWLEDGE").value(9));
    }

    @Test
    void getMissingActivityReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/activities/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACTIVITY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Activity not found"));
    }

    @Test
    void createDuplicateNameReturnsConflict() throws Exception {
        createActivity("Meditation", 20);

        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Meditation",
                                  "durationMinutes": 20,
                                  "effects": {"MENTAL": 7}
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACTIVITY_NAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Activity name already exists"));
    }

    @Test
    void createInvalidActivityReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "durationMinutes": 1441,
                                  "cost": -1,
                                  "effects": {"PHYSICAL": -1}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name must not be blank")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("durationMinutes must be between 1 and 1440")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("effects.PHYSICAL must be between 0 and 100")));
    }

    @Test
    void updateActivityPartiallyUpdatesFields() throws Exception {
        String id = createActivity("Study", 90);

        mockMvc.perform(put("/api/activities/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "durationMinutes": 45,
                                  "effects": {"KNOWLEDGE": 8, "MENTAL": 4}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Study"))
                .andExpect(jsonPath("$.durationMinutes").value(45))
                .andExpect(jsonPath("$.effects.KNOWLEDGE").value(8))
                .andExpect(jsonPath("$.effects.MENTAL").value(4))
                .andExpect(jsonPath("$.effects.PHYSICAL").value(0));
    }

    @Test
    void updateActivityNameConflictReturnsConflict() throws Exception {
        createActivity("Yoga", 40);
        String id = createActivity("Walk", 30);

        mockMvc.perform(put("/api/activities/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Yoga"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Activity name already exists"));
    }

    @Test
    void deleteActivityRemovesActivity() throws Exception {
        String id = createActivity("Temporary", 15);

        mockMvc.perform(delete("/api/activities/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/activities/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteMissingActivityReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/activities/{id}", 404))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Activity not found"));
    }

    @Test
    void recommendationUsesActivitiesCreatedThroughManagementApi() throws Exception {
        createActivity("API created workout", 30);

        mockMvc.perform(post("/api/activities/recommendation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "availableMinutes": 30,
                                  "priorities": {"PHYSICAL": 3}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activityCount").value(1))
                .andExpect(jsonPath("$.selectedActivities[0].name").value("API created workout"));
    }

    private String createActivity(String name, int durationMinutes) throws Exception {
        String location = mockMvc.perform(post("/api/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "description": "Created in test",
                                  "durationMinutes": %d,
                                  "effects": {"PHYSICAL": 10, "MENTAL": 3}
                                }
                                """.formatted(name, durationMinutes)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        return location.substring(location.lastIndexOf('/') + 1);
    }
}
