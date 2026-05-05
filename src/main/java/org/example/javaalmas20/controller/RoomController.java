package org.example.javaalmas20.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.javaalmas20.dto.request.CreateRoomRequest;
import org.example.javaalmas20.dto.response.RoomResponse;
import org.example.javaalmas20.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Room management controller (teachers create/close, students validate).
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Quiz Room Management API")
@SecurityRequirement(name = "bearerAuth")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new room (teacher only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.createRoom(request, userDetails.getUsername()));
    }

    @GetMapping("/validate/{code}")
    @Operation(summary = "Validate a room code (check if active)")
    public ResponseEntity<RoomResponse> validateRoom(@PathVariable String code) {
        return ResponseEntity.ok(roomService.validateRoomCode(code));
    }

    @PostMapping("/{code}/close")
    @Operation(summary = "Close a room (teacher only)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<Void> closeRoom(@PathVariable String code) {
        roomService.closeRoom(code);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @Operation(summary = "Get my active rooms (teacher)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    public ResponseEntity<List<RoomResponse>> getMyRooms(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(roomService.getActiveRooms(userDetails.getUsername()));
    }
}
