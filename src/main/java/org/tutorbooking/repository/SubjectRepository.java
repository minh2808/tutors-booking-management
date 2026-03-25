package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}