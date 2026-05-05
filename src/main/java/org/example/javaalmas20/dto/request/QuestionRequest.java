package org.example.javaalmas20.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating / updating a question.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotBlank(message = "Option 1 is required")
    private String opt1;

    @NotBlank(message = "Option 2 is required")
    private String opt2;

    @NotBlank(message = "Option 3 is required")
    private String opt3;

    @NotBlank(message = "Option 4 is required")
    private String opt4;

    @Min(value = 0, message = "Correct index must be 0–3")
    @Max(value = 3, message = "Correct index must be 0–3")
    private int correctIndex;
}
