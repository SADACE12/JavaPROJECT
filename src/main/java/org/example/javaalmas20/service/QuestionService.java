package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.Question;
import org.example.javaalmas20.dto.request.QuestionRequest;
import org.example.javaalmas20.dto.response.QuestionResponse;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.QuestionMapper;
import org.example.javaalmas20.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Question service — CRUD for quiz questions (replaces QuizManager question methods).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final AuditService auditService;

    public List<QuestionResponse> getAll() {
        return questionRepository.findAllByOrderByCreatedAtAsc()
                .stream()
                .map(questionMapper::toResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse getById(UUID id) {
        Question question = findById(id);
        return questionMapper.toResponse(question);
    }

    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        Question question = questionMapper.toEntity(request);
        Question saved = questionRepository.save(question);
        auditService.logAction("SYSTEM", "QUESTION_CREATED",
                "Question created: " + saved.getQuestionText());
        return questionMapper.toResponse(saved);
    }

    @Transactional
    public QuestionResponse update(UUID id, QuestionRequest request) {
        Question question = findById(id);
        questionMapper.updateEntity(request, question);
        Question saved = questionRepository.save(question);
        auditService.logAction("SYSTEM", "QUESTION_UPDATED",
                "Question updated: " + saved.getId());
        return questionMapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Question question = findById(id);
        questionRepository.delete(question);
        auditService.logAction("SYSTEM", "QUESTION_DELETED",
                "Question deleted: " + id);
    }

    private Question findById(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", id));
    }
}
