package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Review;
import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.repository.ReviewRepository;
import org.tutorbooking.repository.TutorRepository;
import org.tutorbooking.repository.TutorSubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j

public class TutorIndexingService {

    private final TutorRepository tutorRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final ReviewRepository reviewRepository;
    private final VectorStore vectorStore;

    @Async
    // Tạm thời tắt tự động chạy AI Indexing theo yêu cầu
    // @EventListener(ApplicationReadyEvent.class)
    @Transactional(readOnly = true)
    public void indexAllTutors() {
        log.info("Starting background indexing of tutor profiles to VectorStore...");
        
        try {
            List<Tutor> tutors = tutorRepository.findAll();
            
            if (tutors.isEmpty()) {
                log.info("No tutors found to index.");
                return;
            }

            List<Document> documents = new ArrayList<>();

            for (Tutor tutor : tutors) {
                if (!"approved".equalsIgnoreCase(tutor.getApprovalStatus())) {
                    continue;
                }

                List<TutorSubject> subjects = tutorSubjectRepository.findByTutorIdWithSubject(tutor.getId());
                List<Review> reviews = reviewRepository.findByTutorId(tutor.getId(), Pageable.ofSize(20)).getContent();

                String content = buildTutorDocument(tutor, subjects, reviews);

                Map<String, Object> metadata = Map.of(
                    "tutorId", tutor.getId(),
                    "tutorName", tutor.getUser().getFullName(),
                    "type", "tutor_profile"
                );

                documents.add(new Document(content, metadata));
            }

            if (!documents.isEmpty()) {
                vectorStore.add(documents);
                log.info("Indexed {} tutor profiles to VectorStore successfully", documents.size());
            } else {
                log.info("No approved tutors found to index.");
            }

        } catch (Exception e) {
            log.error("Failed to index tutor profiles: {}", e.getMessage(), e);
        }
    }

    private String buildTutorDocument(Tutor tutor, List<TutorSubject> subjects, List<Review> reviews) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Gia sư: ").append(tutor.getUser().getFullName()).append("\n");
        sb.append("Học vấn: ").append(tutor.getEducationLevel() != null ? tutor.getEducationLevel() : "Chưa cập nhật").append("\n");
        sb.append("Kinh nghiệm chuyên môn: ").append(tutor.getExperience() != null ? tutor.getExperience() : "Chưa cập nhật").append("\n");
        sb.append("Bằng cấp đạt được: ").append(tutor.getQualifications() != null ? tutor.getQualifications() : "Chưa cập nhật").append("\n");
        sb.append("Hình thức dạy 1-1: ").append(tutor.getTeachingMode() != null ? tutor.getTeachingMode() : "Cả online và offline").append("\n");
        sb.append("Khu vực nhận dạy: ").append(tutor.getTeachingArea() != null ? tutor.getTeachingArea() : "Linh hoạt").append("\n");
        
        sb.append("Môn học có thể dạy:\n");
        if (subjects != null && !subjects.isEmpty()) {
            for (TutorSubject ts : subjects) {
                sb.append("  - ").append(ts.getSubject().getName())
                  .append(" (lớp ").append(ts.getGradeLevel())
                  .append(") | giá ").append(ts.getPricePerSession()).append("đ/buổi\n");
            }
        } else {
            sb.append("  - Chưa cập nhật môn học\n");
        }
        
        if (reviews != null && !reviews.isEmpty()) {
            double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
            sb.append("Đánh giá trung bình: ").append(String.format("%.1f", avg))
              .append("/5 (").append(reviews.size()).append(" lượt đánh giá)\n");
            sb.append("Nhận xét từ phụ huynh về gia sư này:\n");
            for (Review r : reviews) {
                if (r.getComment() != null && !r.getComment().isBlank()) {
                    sb.append("  - \"").append(r.getComment()).append("\"\n");
                }
            }
        }
        
        return sb.toString();
    }
}
