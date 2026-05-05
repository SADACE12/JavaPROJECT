package org.example.javaalmas20.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.javaalmas20.domain.entity.Room;
import org.example.javaalmas20.dto.request.CreateRoomRequest;
import org.example.javaalmas20.dto.response.RoomResponse;
import org.example.javaalmas20.exception.ResourceNotFoundException;
import org.example.javaalmas20.mapper.RoomMapper;
import org.example.javaalmas20.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Room service — create, validate, close, list rooms (replaces QuizManager room methods).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AuditService auditService;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request, String teacherName) {
        String code = generateUniqueCode();
        Room room = Room.builder()
                .code(code)
                .name(request.getName())
                .createdBy(teacherName)
                .active(true)
                .build();
        Room saved = roomRepository.save(room);
        auditService.logAction(teacherName, "ROOM_CREATED",
                "Room created: " + code + " — " + request.getName());
        return roomMapper.toResponse(saved);
    }

    public RoomResponse validateRoomCode(String code) {
        Room room = roomRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room", "code", code + " (not found or closed)"));
        return roomMapper.toResponse(room);
    }

    @Transactional
    public void closeRoom(String code) {
        Room room = roomRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "code", code));
        room.setActive(false);
        roomRepository.save(room);
        auditService.logAction(room.getCreatedBy(), "ROOM_CLOSED",
                "Room closed: " + code);
    }

    public List<RoomResponse> getActiveRooms(String teacherName) {
        return roomRepository.findByCreatedByAndActiveTrueOrderByCreatedAtDesc(teacherName)
                .stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    private String generateUniqueCode() {
        Random rnd = new Random();
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(rnd.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (roomRepository.existsByCode(code));
        return code;
    }
}
