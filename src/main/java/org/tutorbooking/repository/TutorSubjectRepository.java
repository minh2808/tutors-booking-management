package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.tutorbooking.domain.entity.TutorSubject;

import java.util.List;
import java.util.Optional;

public interface TutorSubjectRepository extends JpaRepository<TutorSubject, Long> {

    // xoá hết subject của tutor
    void deleteByTutorId(Long tutorId);

    // lấy thường (không có subject)
    List<TutorSubject> findByTutorId(Long tutorId);

    // lấy full (có subject)
    @Query("""
                SELECT ts FROM TutorSubject ts
                JOIN FETCH ts.subject
                WHERE ts.tutor.id = :tutorId
            """)
    List<TutorSubject> findByTutorIdWithSubject(@Param("tutorId") Long tutorId);

    Optional<TutorSubject> findByTutorIdAndSubjectIdAndGradeLevel(Long tutorId, Long subjectId, Integer gradeLevel);
}