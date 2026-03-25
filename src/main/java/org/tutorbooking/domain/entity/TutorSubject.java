package org.tutorbooking.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tutor_subjects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    @JsonIgnore
    private Tutor tutor;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Integer gradeLevel;
    private Long pricePerSession;
}
