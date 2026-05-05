package org.example.javaalmas20.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Quiz result response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultResponse {

    private UUID id;
    private String roomCode;
    private String studentName;
    private int score;
    private int total;
    private double percentage;
    private String grade;
    private LocalDateTime submittedAt;
}
