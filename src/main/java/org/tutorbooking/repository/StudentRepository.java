package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorbooking.domain.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
