package org.tutorbooking.service;

import org.tutorbooking.dto.response.ChatMessageResponse;
import org.tutorbooking.dto.response.ChatResponse;

import java.util.List;

public interface ChatService {
    ChatResponse chat(Long userId, String message);
    List<ChatMessageResponse> getHistory(Long userId);
    void clearHistory(Long userId);
}
