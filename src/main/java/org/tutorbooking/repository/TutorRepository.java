package org.tutorbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tutorbooking.domain.entity.Tutor;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
}
