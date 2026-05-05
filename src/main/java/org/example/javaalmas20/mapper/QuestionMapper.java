package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Question;
import org.example.javaalmas20.dto.request.QuestionRequest;
import org.example.javaalmas20.dto.response.QuestionResponse;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper: Question entity ↔ DTOs.
 */
@Mapper(componentModel = "spring")
public interface QuestionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question toEntity(QuestionRequest request);

    @Mapping(target = "options", expression = "java(mapOptions(question))")
    QuestionResponse toResponse(Question question);

    /**
     * Updates an existing entity from a request DTO.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(QuestionRequest request, @MappingTarget Question question);

    default List<String> mapOptions(Question q) {
        return List.of(q.getOpt1(), q.getOpt2(), q.getOpt3(), q.getOpt4());
    }
}
