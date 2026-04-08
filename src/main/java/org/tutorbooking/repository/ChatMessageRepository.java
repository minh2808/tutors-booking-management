package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.ChatMessage;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Modifying
    void deleteByUserId(Long userId);
}
