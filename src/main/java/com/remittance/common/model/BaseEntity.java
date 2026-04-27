package com.remittance.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.UUID;

// To tell Hibernate to push these columns down to tables
@MappedSuperclass
//Tells Spring to automatically update the timestamps when saving
@EntityListeners(AuditingEntityListener.class)

// Lombok annotations replacing manual methods!
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public abstract class BaseEntity {

    //UUID Generation
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    //Automatically set when the row is first inserted
    @CreatedDate
    @Column(name= "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    //Automatically updated every time the row change
    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
