package org.example.javaalmas20.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quiz result entity — stores a student's test outcome for a room.
 */
@Entity
@Table(name = "results", indexes = {
        @Index(name = "idx_result_room", columnList = "roomCode"),
        @Index(name = "idx_result_student", columnList = "studentName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "room_code", nullable = false, length = 10)
    private String roomCode;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private double percentage;

    /** Grade: A / B / C / D / F. */
    @Column(nullable = false, length = 2)
    private String grade;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}
