package com.neo.SimpleMicroService.service;

import com.neo.SimpleMicroService.dto.SimpleDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Slf4j
@Service
public class SimpleService {

    private static final int INITIAL_COUNT = 11;
    private final AtomicLong idSequence = new AtomicLong(1L);
    private final List<SimpleDto> simpleDtoList = new CopyOnWriteArrayList<>();

    @PostConstruct
    private void initialize() {
        log.info("Initializing SimpleService with {} default records...", INITIAL_COUNT);
        IntStream.range(0, INITIAL_COUNT).forEach(i -> this.create());
        log.info("Initialization complete. Current sequence ID is {}", idSequence.get());
    }

    public boolean healthCheckInternal() {
        log.debug("Internal health check invoked");
        return true;
    }

    public SimpleDto create() {
        long newId = idSequence.getAndIncrement();
        SimpleDto simpleDto = new SimpleDto(newId, "Neo_" + newId, "Neo_" + newId + "@gmail.com");
        this.simpleDtoList.add(simpleDto);

        log.info("Created default SimpleDto with ID: {}", newId);
        return simpleDto;
    }

    public SimpleDto create(SimpleDto incomingDto) {
        if (incomingDto == null) {
            log.warn("Creation failed: Incoming SimpleDto payload is null");
            throw new IllegalArgumentException("Payload cannot be null");
        }

        long assignedId = incomingDto.id() != null ? incomingDto.id() : idSequence.getAndIncrement();
        SimpleDto finalizedDto = new SimpleDto(assignedId, incomingDto.name(), incomingDto.email());
        this.simpleDtoList.add(finalizedDto);

        log.info("Created custom SimpleDto with ID: {}", assignedId);
        return finalizedDto;
    }

    public Optional<SimpleDto> get(Long id) {
        if (id == null) {
            log.warn("Fetch attempt with null ID");
            return Optional.empty();
        }

        log.debug("Fetching SimpleDto by ID: {}", id);
        Optional<SimpleDto> result = simpleDtoList.stream()
                .filter(dtoObj -> dtoObj != null && Objects.equals(dtoObj.id(), id))
                .findFirst();

        if (result.isEmpty()) {
            log.debug("No SimpleDto found for ID: {}", id);
        }

        return result;
    }

    public List<SimpleDto> getByName(String name) {
        if (this.simpleDtoList.isEmpty()) {
            log.debug("getByName invoked but list is empty");
            return List.of();
        }

        if (name == null || name.isBlank()) {
            log.debug("getByName invoked with blank/null name, returning full list. Size: {}", this.simpleDtoList.size());
            return List.copyOf(this.simpleDtoList);
        }

        log.debug("Fetching SimpleDto starting with name: '{}'", name);
        List<SimpleDto> filteredList = this.simpleDtoList.stream()
                .filter(dto -> dto != null && dto.name() != null)
                .filter(dto -> dto.name().startsWith(name))
                .toList();

        log.debug("Found {} matches for name: '{}'", filteredList.size(), name);
        return filteredList;
    }

    public List<SimpleDto> getAll() {
        log.debug("Fetching all SimpleDtos. Current size: {}", this.simpleDtoList.size());
        return List.copyOf(this.simpleDtoList);
    }
}