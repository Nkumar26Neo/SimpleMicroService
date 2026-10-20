package com.neo.SimpleMicroService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neo.SimpleMicroService.dto.SimpleDto;
import com.neo.SimpleMicroService.exception.GlobalExceptionHandler;
import com.neo.SimpleMicroService.service.SimpleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimpleController.class)
@Import(GlobalExceptionHandler.class)
class SimpleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SimpleService simpleService;

    @Test
    @DisplayName("GET /api/v1/simples/health returns 200 OK")
    void testHealthCheck() throws Exception {
        when(simpleService.healthCheckInternal()).thenReturn(true);

        mockMvc.perform(get("/api/v1/simples/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/simples/{id} returns 200 when found")
    void testGetByIdFound() throws Exception {
        SimpleDto dto = new SimpleDto(1L, "Alice", "alice@example.com");
        when(simpleService.get(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/simples/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /api/v1/simples/{id} returns 404 when not found")
    void testGetByIdNotFound() throws Exception {
        when(simpleService.get(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/simples/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("SimpleDto not found with id: 99"));
    }

    @Test
    @DisplayName("POST /api/v1/simples creates record and returns 201 Created")
    void testCreateSuccess() throws Exception {
        SimpleDto payload = new SimpleDto(null, "Charlie", "charlie@example.com");
        SimpleDto saved = new SimpleDto(15L, "Charlie", "charlie@example.com");

        when(simpleService.create(any(SimpleDto.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/simples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"));
    }
}