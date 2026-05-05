package org.example.javaalmas20.mapper;

import org.example.javaalmas20.domain.entity.Room;
import org.example.javaalmas20.dto.response.RoomResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "active", target = "active")
    RoomResponse toResponse(Room room);
}
