package com.remittance.user.entity;

import com.remittance.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name="users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
}
