package com.neo.SimpleMicroService.service;

import com.neo.SimpleMicroService.dto.SimpleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleServiceTest {

    private SimpleService simpleService;

    @BeforeEach
    void setUp() throws Exception {
        simpleService = new SimpleService();
        // Invoke @PostConstruct initialize() method manually for standalone unit testing
        Method initMethod = SimpleService.class.getDeclaredMethod("initialize");
        initMethod.setAccessible(true);
        initMethod.invoke(simpleService);
    }

    @Test
    @DisplayName("Initialization should populate exactly 11 records with sequential IDs")
    void testInitialize() {
        List<SimpleDto> all = simpleService.getAll();
        assertThat(all).hasSize(11);
        assertThat(all.get(0).id()).isEqualTo(1L);
        assertThat(all.get(10).id()).isEqualTo(11L);
    }

    @Nested
    @DisplayName("Create operations")
    class CreateTests {

        @Test
        @DisplayName("create() auto-generates sequential ID and default email")
        void testCreateAuto() {
            SimpleDto created = simpleService.create();

            assertThat(created.id()).isEqualTo(12L);
            assertThat(created.name()).isEqualTo("Neo_12");
            assertThat(created.email()).isEqualTo("Neo_12@gmail.com");
            assertThat(simpleService.getAll()).hasSize(12);
        }

        @Test
        @DisplayName("create(SimpleDto) preserves assigned ID if present")
        void testCreateWithProvidedId() {
            SimpleDto incoming = new SimpleDto(999L, "Custom", "custom@example.com");
            SimpleDto created = simpleService.create(incoming);

            assertThat(created.id()).isEqualTo(999L);
            assertThat(created.name()).isEqualTo("Custom");
            assertThat(simpleService.get(999L)).isPresent();
        }

        @Test
        @DisplayName("create(SimpleDto) assigns new sequential ID when incoming ID is null")
        void testCreateWithNullIdGeneratesNextSequence() {
            SimpleDto incoming = new SimpleDto(null, "NoId", "noid@example.com");
            SimpleDto created = simpleService.create(incoming);

            assertThat(created.id()).isEqualTo(12L);
            assertThat(created.name()).isEqualTo("NoId");
        }

        @Test
        @DisplayName("create(null) throws IllegalArgumentException")
        void testCreateNullThrowsException() {
            assertThatThrownBy(() -> simpleService.create(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Payload cannot be null");
        }
    }

    @Nested
    @DisplayName("Query operations")
    class QueryTests {

        @Test
        @DisplayName("get(id) returns matching record if exists")
        void testGetExistingId() {
            Optional<SimpleDto> result = simpleService.get(1L);
            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Neo_1");
        }

        @Test
        @DisplayName("get(id) returns empty Optional for non-existent ID or null ID")
        void testGetNonExistentOrNullId() {
            assertThat(simpleService.get(9999L)).isEmpty();
            assertThat(simpleService.get(null)).isEmpty();
        }

        @Test
        @DisplayName("getByName(name) returns prefix-matching records")
        void testGetByNamePrefix() {
            List<SimpleDto> results = simpleService.getByName("Neo_1");
            // Neo_1, Neo_10, Neo_11
            assertThat(results).extracting(SimpleDto::name)
                    .contains("Neo_1", "Neo_10", "Neo_11");
        }

        @Test
        @DisplayName("getByName(null) or getByName('') returns all records")
        void testGetByNameBlankReturnsAll() {
            assertThat(simpleService.getByName(null)).hasSize(11);
            assertThat(simpleService.getByName("   ")).hasSize(11);
        }
    }
}