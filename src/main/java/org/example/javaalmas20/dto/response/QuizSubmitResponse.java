package org.example.javaalmas20.dto.response;

import lombok.*;

import java.util.List;

/**
 * Response after a student submits a quiz — shows score, grade, and per-question breakdown.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitResponse {

    private String studentName;
    private String roomCode;
    private int score;
    private int total;
    private double percentage;
    private String grade;

    /** Per-question breakdown: was the answer correct? */
    private List<QuestionBreakdown> breakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionBreakdown {
        private String questionText;
        private String userAnswer;
        private String correctAnswer;
        private boolean correct;
    }
}
