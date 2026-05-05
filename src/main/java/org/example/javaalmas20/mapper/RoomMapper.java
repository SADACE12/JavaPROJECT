package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Room;
import org.example.javaalmas20.dto.response.RoomResponse;
import org.springframework.stereotype.Component;

/**
 * Manual mapper: Room ↔ DTO.
 */
@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .code(room.getCode())
                .name(room.getName())
                .createdBy(room.getCreatedBy())
                .active(room.isActive())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
