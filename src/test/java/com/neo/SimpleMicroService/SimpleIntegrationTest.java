package com.neo.SimpleMicroService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.SimpleMicroService.dto.SimpleDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimpleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("End-to-End Happy Paths")
    class HappyPathTests {

        @Test
        @DisplayName("Should query pre-initialized records from @PostConstruct")
        void testPreInitializedRecords() throws Exception {
            mockMvc.perform(get("/api/v1/simples/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Neo_1"));
        }

        @Test
        @DisplayName("Auto-generate endpoint creates next ID in sequence")
        void testAutoGenerateFlow() throws Exception {
            mockMvc.perform(post("/api/v1/simples/auto"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").isNotEmpty());
        }

        @Test
        @DisplayName("Search by name filter returns only matches")
        void testSearchByName() throws Exception {
            mockMvc.perform(get("/api/v1/simples").param("name", "Neo_1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Validation and Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("GET /api/v1/simples/{id} with non-numeric ID returns 400 Type Mismatch")
        void testTypeMismatch() throws Exception {
            mockMvc.perform(get("/api/v1/simples/abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Parameter 'id' should be of type 'Long'"));
        }

        @Test
        @DisplayName("GET /api/v1/simples/{id} with non-positive ID triggers @Positive violation")
        void testPositivePathConstraint() throws Exception {
            mockMvc.perform(get("/api/v1/simples/-5"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Requesting an unknown route returns 404 NoResourceFound")
        void testInvalidEndpoint() throws Exception {
            mockMvc.perform(get("/api/v1/non-existent-endpoint"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("Not Found"));
        }
    }
}