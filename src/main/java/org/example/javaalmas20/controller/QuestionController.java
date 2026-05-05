package org.example.javaalmas20.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.javaalmas20.dto.request.QuestionRequest;
import org.example.javaalmas20.dto.response.QuestionResponse;
import org.example.javaalmas20.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Question CRUD controller (teacher-managed).
 */
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Quiz Question Management API")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @Operation(summary = "Get all questions (Admin/Moderator)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<List<QuestionResponse>> getAll() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @GetMapping("/public")
    @Operation(summary = "Get all questions (Public for students)")
    public ResponseEntity<List<QuestionResponse>> getAllPublic() {
        return ResponseEntity.ok(questionService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a question by ID")
    public ResponseEntity<QuestionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(questionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new question (teacher only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question (teacher only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<QuestionResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question (teacher only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'USER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
