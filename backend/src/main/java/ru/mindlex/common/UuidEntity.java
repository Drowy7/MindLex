package ru.mindlex.common;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Базовая сущность с идентификатором типа UUID.
 * Идентификатор генерируется приложением, а не базой данных, — это делает
 * поведение одинаковым в PostgreSQL и во встроенной СУБД тестов (фактор X).
 */
@MappedSuperclass
public abstract class UuidEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @PrePersist
    void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == null;
    }
}
