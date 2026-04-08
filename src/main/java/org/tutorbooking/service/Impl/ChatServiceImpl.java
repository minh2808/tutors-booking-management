package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.ChatMessage;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.domain.enums.ChatRole;
import org.tutorbooking.dto.response.ChatMessageResponse;
import org.tutorbooking.dto.response.ChatResponse;
import org.tutorbooking.repository.ChatMessageRepository;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.ChatService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final VectorStore vectorStore;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    private static final String SYSTEM_PROMPT = """
        Bạn là hệ thống AI tư vấn giáo dục của nền tảng GIASUPRO - 
        Nơi kết nối gia sư và phụ huynh tìm kiếm giáo viên.
        
        BẠN CÓ THỂ:
        - Tư vấn chọn gia sư phù hợp nhất dựa trên thông tin hồ sơ bên dưới.
        - Lên lộ trình học tập và tư vấn môn/lớp.
        - Gợi ý phương pháp học hiệu quả.
        
        BẠN KHÔNG ĐƯỢC PHÉP:
        - Bịa đặt hoặc hư cấu thông tin gia sư không tồn tại trong danh sách dữ liệu.
        - Trả lời các chủ đề chính trị, ngoài lề giáo dục.
        - Giải bài tập về nhà hộ học sinh.
        
        NGUYÊN TẮC TRẢ LỜI: 
        Trả lời bằng tiếng Việt, xưng hô 'hệ thống' hoặc 'tư vấn viên GIASUPRO'. Thân thiện và mạch lạc.
        Khi gợi ý Gia sư, luôn luôn giải thích RÕ RÀNG TẠI SAO gia sư này lại phù hợp với nhu cầu.
        """;

    @Override
    @Transactional
    public ChatResponse chat(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Document> relevantDocs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(message)
                .topK(5)
                .similarityThreshold(0.3)
                .build()
        );
        
        String context = relevantDocs.isEmpty() 
            ? "Không tìm thấy gia sư cụ thể nào phù hợp trong hệ thống lúc này. Tuy nhiên, vẫn có thể trả lời các kiến thức thông thường."
            : relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));

        List<ChatMessage> history = chatMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        Collections.reverse(history); // Đảo ngược để xếp theo thứ tự cũ nhất -> mới nhất

        String userContext = String.format(
            "Người dùng hiện tại: %s (Vai trò: %s)\n\n" +
            "THÔNG TIN GIA SƯ LIÊN QUAN TỪ HỆ THỐNG:\n%s",
            user.getFullName(), user.getRole(), context
        );

        ChatClient chatClient = chatClientBuilder.build();
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT + "\n\n" + userContext));

        for (ChatMessage msg : history) {
            if (msg.getRole() == ChatRole.USER) {
                messages.add(new UserMessage(msg.getContent()));
            } else {
                messages.add(new AssistantMessage(msg.getContent()));
            }
        }
        messages.add(new UserMessage(message));

        String reply;
        try {
            reply = chatClient.prompt()
                .messages(messages)
                .call()
                .content();
        } catch (Exception e) {
            log.error("AI API call failed", e);
            reply = "Hệ thống AI hiện đang bận hoặc quá tải (Gemini Rate Limit). Xin vui lòng thử lại sau ít phút.";
        }

        saveMessage(user, ChatRole.USER, message);
        saveMessage(user, ChatRole.ASSISTANT, reply);

        return ChatResponse.builder()
            .reply(reply)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public List<ChatMessageResponse> getHistory(Long userId) {
        List<ChatMessage> history = chatMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        return history.stream()
            .map(msg -> ChatMessageResponse.builder()
                .role(msg.getRole())
                .content(msg.getContent())
                .timestamp(msg.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearHistory(Long userId) {
        chatMessageRepository.deleteByUserId(userId);
    }

    private void saveMessage(User user, ChatRole role, String content) {
        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(role)
                .content(content)
                .build();
        chatMessageRepository.save(message);
    }
}
