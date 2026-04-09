package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.domain.entity.Subject;
import org.tutorbooking.dto.request.SubjectCreateRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.service.SubjectService;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    // #23: Lấy danh sách tất cả môn học (Public)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Subject>>> getAllSubjects() {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn học thành công", subjectService.getAllSubjects()));
    }

    // #24: Admin tạo môn học mới
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Subject>> createSubject(@Valid @RequestBody SubjectCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo môn học thành công", subjectService.createSubject(request)));
    }

    // #25: Admin sửa thông tin môn học
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Subject>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn học thành công", subjectService.updateSubject(id, request)));
    }

    // #26: Admin xóa môn học
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa môn học thành công", null));
    }
}
