package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Question;
import org.example.javaalmas20.dto.request.QuestionRequest;
import org.example.javaalmas20.dto.response.QuestionResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper: Question entity ↔ DTOs.
 */
@Component
public class QuestionMapper {

    public Question toEntity(QuestionRequest request) {
        return Question.builder()
                .questionText(request.getQuestionText())
                .opt1(request.getOpt1())
                .opt2(request.getOpt2())
                .opt3(request.getOpt3())
                .opt4(request.getOpt4())
                .correctIndex(request.getCorrectIndex())
                .build();
    }

    public QuestionResponse toResponse(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(List.of(
                        question.getOpt1(),
                        question.getOpt2(),
                        question.getOpt3(),
                        question.getOpt4()))
                .correctIndex(question.getCorrectIndex())
                .createdAt(question.getCreatedAt())
                .build();
    }

    public void updateEntity(QuestionRequest request, Question question) {
        question.setQuestionText(request.getQuestionText());
        question.setOpt1(request.getOpt1());
        question.setOpt2(request.getOpt2());
        question.setOpt3(request.getOpt3());
        question.setOpt4(request.getOpt4());
        question.setCorrectIndex(request.getCorrectIndex());
    }
}
