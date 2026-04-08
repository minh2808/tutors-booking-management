package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.ChatRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.ChatMessageResponse;
import org.tutorbooking.dto.response.ChatResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(user.getId(), request.getMessage());
        return ResponseEntity.ok(ApiResponse.success("Success", response));
    }

    @GetMapping("/chat/history")
    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> history(
            @AuthenticationPrincipal UserPrincipal user) {
        List<ChatMessageResponse> history = chatService.getHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Success", history));
    }

    @DeleteMapping("/chat/history")
    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    public ResponseEntity<ApiResponse<Void>> clear(
            @AuthenticationPrincipal UserPrincipal user) {
        chatService.clearHistory(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Chat history cleared", null));
    }
}
