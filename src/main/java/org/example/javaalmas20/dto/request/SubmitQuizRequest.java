package org.example.javaalmas20.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Request DTO for a student submitting quiz answers.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuizRequest {

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Room code is required")
    private String roomCode;

    /** Ordered list of answer indices (0–3), one per question. */
    @NotNull(message = "Answers are required")
    private List<Integer> answers;
}
