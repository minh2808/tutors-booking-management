package org.tutorbooking.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Parent;
import org.tutorbooking.domain.entity.Student;
import org.tutorbooking.dto.request.StudentRequest;
import org.tutorbooking.dto.response.StudentResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.ParentRepository;
import org.tutorbooking.repository.StudentRepository;
import org.tutorbooking.service.StudentService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    private Parent getParentEntityByUser(Long userId) {
        return parentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản của bạn không có hồ sơ phụ huynh."));
    }

    @Override
    public List<StudentResponse> getStudentsByParent(Long parentUserId) {
        Parent parent = getParentEntityByUser(parentUserId);
        List<Student> students = studentRepository.findByParentId(parent.getId());
        return students.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentResponse addStudent(Long parentUserId, StudentRequest request) {
        Parent parent = getParentEntityByUser(parentUserId);
        
        Student student = new Student();
        student.setParent(parent);
        student.setFullName(request.getFullName());
        student.setGrade(request.getGrade());
        student.setSchool(request.getSchool());
        student.setAcademicLevel(request.getAcademicLevel());
        student.setSpecialNotes(request.getSpecialNotes());
        
        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long parentUserId, Long studentId, StudentRequest request) {
        Parent parent = getParentEntityByUser(parentUserId);
        
        Student student = studentRepository.findByIdAndParentId(studentId, parent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin học sinh hoặc bạn không có quyền sửa."));
                
        student.setFullName(request.getFullName());
        student.setGrade(request.getGrade());
        student.setSchool(request.getSchool());
        student.setAcademicLevel(request.getAcademicLevel());
        student.setSpecialNotes(request.getSpecialNotes());
        
        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long parentUserId, Long studentId) {
        Parent parent = getParentEntityByUser(parentUserId);
        
        Student student = studentRepository.findByIdAndParentId(studentId, parent.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin học sinh cần xóa."));
                
        studentRepository.delete(student);
    }
    
    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .fullName(student.getFullName())
                .grade(student.getGrade())
                .school(student.getSchool())
                .academicLevel(student.getAcademicLevel())
                .specialNotes(student.getSpecialNotes())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
