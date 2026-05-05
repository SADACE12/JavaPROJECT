package org.example.javaalmas20.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Room entity — a quiz session created by a teacher.
 * Students join using a 6-character room code.
 */
@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_code", columnList = "code", unique = true),
        @Index(name = "idx_room_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Unique 6-character alphanumeric code (e.g. "AB12CD"). */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    /** Display name (e.g. "Тест по теме 1"). */
    @Column(nullable = false)
    private String name;

    /** Teacher who created this room. */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
