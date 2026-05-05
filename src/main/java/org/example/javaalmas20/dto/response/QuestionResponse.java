package org.example.javaalmas20.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Question response DTO — returns options as a list.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {

    private UUID id;
    private String questionText;
    private List<String> options;
    private int correctIndex;
    private LocalDateTime createdAt;
}
