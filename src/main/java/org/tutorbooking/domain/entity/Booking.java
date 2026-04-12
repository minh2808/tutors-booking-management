package org.tutorbooking.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.TeachingMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "grade_level", nullable = false)
    private Byte gradeLevel;

    @Column(name = "price_per_session", nullable = false, precision = 10, scale = 0)
    private BigDecimal pricePerSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "teaching_mode", nullable = false)
    private TeachingMode teachingMode;

    @Column(name = "is_recurring")
    private Boolean isRecurring;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BookingSchedule> schedules = new java.util.ArrayList<>();

    @Column(name = "recurring_start_date")
    private LocalDate recurringStartDate;

    @Column(name = "recurring_end_date")
    private LocalDate recurringEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
