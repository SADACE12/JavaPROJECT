package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.QuizResult;
import org.example.javaalmas20.dto.response.QuizResultResponse;
import org.springframework.stereotype.Component;

/**
 * Manual mapper: QuizResult ↔ DTO.
 */
@Component
public class QuizResultMapper {

    public QuizResultResponse toResponse(QuizResult result) {
        return QuizResultResponse.builder()
                .id(result.getId())
                .roomCode(result.getRoomCode())
                .studentName(result.getStudentName())
                .score(result.getScore())
                .total(result.getTotal())
                .percentage(result.getPercentage())
                .grade(result.getGrade())
                .submittedAt(result.getSubmittedAt())
                .build();
    }
}
