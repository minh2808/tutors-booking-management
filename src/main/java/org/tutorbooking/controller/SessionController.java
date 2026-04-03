package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.domain.enums.SessionStatus;
import org.tutorbooking.dto.request.SessionCancelRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.SessionDetailResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.SessionService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SessionDetailResponse>>> getSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<SessionDetailResponse> sessions = sessionService.getSessions(
                userPrincipal.getId(), userPrincipal.getRole(),
                startDate, endDate, status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SessionDetailResponse>> getSessionById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        SessionDetailResponse session = sessionService.getSessionById(
                userPrincipal.getId(), userPrincipal.getRole(), id);
        return ResponseEntity.ok(ApiResponse.success("Session retrieved successfully", session));
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<SessionDetailResponse>> confirmSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        SessionDetailResponse session = sessionService.confirmSession(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Session confirmed successfully", session));
    }

    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<SessionDetailResponse>> cancelSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody SessionCancelRequest request) {

        SessionDetailResponse session = sessionService.cancelSession(
                userPrincipal.getId(), id, request.getCancelReason());
        return ResponseEntity.ok(ApiResponse.success("Session cancelled successfully", session));
    }
}
