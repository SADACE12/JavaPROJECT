package org.example.javaalmas20.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Room response DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private UUID id;
    private String code;
    private String name;
    private String createdBy;
    private boolean active;
    private LocalDateTime createdAt;
}
