package org.tutorbooking.service;

import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.dto.request.SubjectCreateRequest;

import java.util.List;

public interface SubjectService {
    
    List<Subject> getAllSubjects();

    Subject createSubject(SubjectCreateRequest request);

    Subject updateSubject(Long id, SubjectCreateRequest request);

    void deleteSubject(Long id);
}
