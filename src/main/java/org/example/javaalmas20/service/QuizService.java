package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.Question;
import org.example.javaalmas20.domain.entity.QuizResult;
import org.example.javaalmas20.dto.request.SubmitQuizRequest;
import org.example.javaalmas20.dto.response.QuizResultResponse;
import org.example.javaalmas20.dto.response.QuizSubmitResponse;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.QuizResultMapper;
import org.example.javaalmas20.repository.QuestionRepository;
import org.example.javaalmas20.repository.QuizResultRepository;
import org.example.javaalmas20.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Quiz service — handles quiz submission, grading, and results retrieval
 * (replaces QuizManager.saveResult / getResultsForRoom / calcGrade).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final RoomRepository roomRepository;
    private final QuizResultMapper quizResultMapper;
    private final AuditService auditService;

    /**
     * Submit quiz answers, grade them, save result, return detailed breakdown.
     */
    @Transactional
    public QuizSubmitResponse submitQuiz(SubmitQuizRequest request) {
        // Validate room exists and is active
        String code = request.getRoomCode().toUpperCase();
        roomRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room", "code", code + " (not found or closed)"));

        // Load all questions in order
        List<Question> questions = questionRepository.findAllByOrderByCreatedAtAsc();
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions available in the quiz");
        }

        List<Integer> answers = request.getAnswers();
        int total = questions.size();
        int score = 0;
        List<QuizSubmitResponse.QuestionBreakdown> breakdown = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            Question q = questions.get(i);
            int userAns = (i < answers.size()) ? answers.get(i) : -1;
            boolean isCorrect = (userAns == q.getCorrectIndex());
            if (isCorrect) score++;

            List<String> opts = List.of(q.getOpt1(), q.getOpt2(), q.getOpt3(), q.getOpt4());
            String userAnswer = (userAns >= 0 && userAns < 4) ? opts.get(userAns) : "Нет ответа";
            String correctAnswer = opts.get(q.getCorrectIndex());

            breakdown.add(QuizSubmitResponse.QuestionBreakdown.builder()
                    .questionText(q.getQuestionText())
                    .userAnswer(userAnswer)
                    .correctAnswer(correctAnswer)
                    .correct(isCorrect)
                    .build());
        }

        double percentage = total > 0 ? (double) score / total * 100 : 0;
        String grade = calcGrade(percentage);

        // Save result to DB
        QuizResult result = QuizResult.builder()
                .roomCode(code)
                .studentName(request.getStudentName())
                .score(score)
                .total(total)
                .percentage(percentage)
                .grade(grade)
                .build();
        quizResultRepository.save(result);

        auditService.logAction(request.getStudentName(), "QUIZ_SUBMITTED",
                "Student " + request.getStudentName() + " scored " + score + "/" + total
                        + " (" + grade + ") in room " + code);

        return QuizSubmitResponse.builder()
                .studentName(request.getStudentName())
                .roomCode(code)
                .score(score)
                .total(total)
                .percentage(percentage)
                .grade(grade)
                .breakdown(breakdown)
                .build();
    }

    /**
     * Get results for a room (teacher view) — sorted by percentage descending.
     */
    public List<QuizResultResponse> getResultsForRoom(String roomCode) {
        return quizResultRepository.findByRoomCodeOrderByPercentageDesc(roomCode.toUpperCase())
                .stream()
                .map(quizResultMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get results for a student — sorted by submission time descending.
     */
    public List<QuizResultResponse> getResultsForStudent(String studentName) {
        return quizResultRepository.findByStudentNameOrderBySubmittedAtDesc(studentName)
                .stream()
                .map(quizResultMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Grade calculation — same logic as the old QuizManager.calcGrade.
     */
    public String calcGrade(double percentage) {
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }
}
