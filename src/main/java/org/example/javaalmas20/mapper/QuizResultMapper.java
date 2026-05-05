package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.QuizResult;
import org.example.javaalmas20.dto.response.QuizResultResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuizResultMapper {

    QuizResultResponse toResponse(QuizResult result);
}
