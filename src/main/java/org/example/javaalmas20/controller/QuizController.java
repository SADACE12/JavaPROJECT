package org.example.javaalmas20.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.javaalmas20.dto.request.SubmitQuizRequest;
import org.example.javaalmas20.dto.response.QuizResultResponse;
import org.example.javaalmas20.dto.response.QuizSubmitResponse;
import org.example.javaalmas20.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Quiz controller — submit answers, view results.
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz Submission & Results API")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/submit")
    @Operation(summary = "Submit quiz answers and get graded result")
    public ResponseEntity<QuizSubmitResponse> submitQuiz(@Valid @RequestBody SubmitQuizRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(request));
    }

    @GetMapping("/results/room/{roomCode}")
    @Operation(summary = "Get all results for a room (teacher view)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<QuizResultResponse>> getResultsForRoom(@PathVariable String roomCode) {
        return ResponseEntity.ok(quizService.getResultsForRoom(roomCode));
    }

    @GetMapping("/results/student/{studentName}")
    @Operation(summary = "Get results for a specific student")
    public ResponseEntity<List<QuizResultResponse>> getResultsForStudent(@PathVariable String studentName) {
        return ResponseEntity.ok(quizService.getResultsForStudent(studentName));
    }
}
