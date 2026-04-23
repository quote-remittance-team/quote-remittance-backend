package com.remittance.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

// To tell Hibernate to push these columns down to tables
@MappedSuperclass
//Tells Spring to automatically update the timestamps when saving
@EntityListeners(AuditingEntityListener.class)

public abstract class BaseEntity {

    //UUID Generation
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    //Automatically set when the row is first inserted
    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    //Automatically updated every time the row change
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // GETTERS AND SETTERS
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
