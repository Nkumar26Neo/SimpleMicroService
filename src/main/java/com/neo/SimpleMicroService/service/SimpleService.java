package com.neo.SimpleMicroService.service;

import com.neo.SimpleMicroService.dto.SimpleDto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Service
public class SimpleService {

    private static final int INITIAL_COUNT = 11;
    private final AtomicLong idSequence = new AtomicLong(1L);
    private final List<SimpleDto> simpleDtoList = new CopyOnWriteArrayList<>();

    @PostConstruct
    private void initialize() {
        IntStream.range(0, INITIAL_COUNT).forEach(i -> this.create());
    }

    public boolean healthCheckInternal() {
        return true;
    }

    public SimpleDto create() {
        long newId = idSequence.getAndIncrement();
        SimpleDto simpleDto = new SimpleDto(newId, "Neo_" + newId, "Neo_" + newId + "@gmail.com");
        this.simpleDtoList.add(simpleDto);
        return simpleDto;
    }

    public SimpleDto create(SimpleDto incomingDto) {
        if (incomingDto == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        long assignedId = incomingDto.id() != null ? incomingDto.id() : idSequence.getAndIncrement();
        SimpleDto finalizedDto = new SimpleDto(assignedId, incomingDto.name(), incomingDto.email());
        this.simpleDtoList.add(finalizedDto);
        return finalizedDto;
    }

    public Optional<SimpleDto> get(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return simpleDtoList.stream()
                .filter(dtoObj -> dtoObj != null && Objects.equals(dtoObj.id(), id))
                .findFirst();
    }

    public List<SimpleDto> getByName(String name) {
        if (this.simpleDtoList.isEmpty()) {
            return List.of();
        }

        if (name == null || name.isBlank()) {
            return List.copyOf(this.simpleDtoList);
        }

        return this.simpleDtoList.stream()
                .filter(dto -> dto != null && dto.name() != null)
                .filter(dto -> dto.name().startsWith(name))
                .toList();
    }

    public List<SimpleDto> getAll() {
        return List.copyOf(this.simpleDtoList);
    }
}