package org.tutorbooking.service;

import org.tutorbooking.dto.request.StudentRequest;
import org.tutorbooking.dto.response.StudentResponse;

import java.util.List;

public interface StudentService {
    List<StudentResponse> getStudentsByParent(Long parentUserId);
    StudentResponse addStudent(Long parentUserId, StudentRequest request);
    StudentResponse updateStudent(Long parentUserId, Long studentId, StudentRequest request);
    void deleteStudent(Long parentUserId, Long studentId);
}
