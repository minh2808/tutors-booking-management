package org.tutorbooking.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.tutorbooking.domain.enums.TeachingMode;
import org.tutorbooking.domain.enums.TutorRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tutor_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "grade_level", nullable = false)
    private Byte gradeLevel;

    @Column(name = "desired_price", precision = 10, scale = 0)
    private BigDecimal desiredPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "teaching_mode", nullable = false)
    private TeachingMode teachingMode;

    @Column(name = "preferred_area", length = 255)
    private String preferredArea;

    @Column(name = "schedule_note", columnDefinition = "TEXT")
    private String scheduleNote;

    @Column(name = "sessions_per_week")
    private Byte sessionsPerWeek = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TutorRequestStatus status = TutorRequestStatus.PENDING;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
