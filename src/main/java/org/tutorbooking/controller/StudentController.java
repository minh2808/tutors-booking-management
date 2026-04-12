package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.StudentRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.StudentResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.StudentService;

import java.util.List;

@RestController
@RequestMapping("/api/parents/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PreAuthorize("hasRole('PARENT')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getMyStudents(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách học viên thành công", 
                                studentService.getStudentsByParent(principal.getId())));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> addStudent(
            Authentication authentication,
            @Valid @RequestBody StudentRequest request) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Thêm hồ sơ học viên thành công", 
                                studentService.addStudent(principal.getId(), request)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PutMapping("/{studentId}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(
            Authentication authentication,
            @PathVariable Long studentId,
            @Valid @RequestBody StudentRequest request) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin học viên thành công", 
                                studentService.updateStudent(principal.getId(), studentId, request)));
    }

    @PreAuthorize("hasRole('PARENT')")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(
            Authentication authentication,
            @PathVariable Long studentId) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        studentService.deleteStudent(principal.getId(), studentId);
        return ResponseEntity.ok(ApiResponse.success("Xóa hồ sơ học viên thành công", null));
    }
}
