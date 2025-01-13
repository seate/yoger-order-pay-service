package com.project.yogerOrder.global.util.outbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OutboxEntity {

    @Id
    @NotBlank
    protected String eventId;

    @NotBlank
    protected String eventType;

    @NotBlank
    private String payload;

    @CreatedDate
    private LocalDateTime occurrenceTime;


    protected OutboxEntity(String eventType, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
    }
}
