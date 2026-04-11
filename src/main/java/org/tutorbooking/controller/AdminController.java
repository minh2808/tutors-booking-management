package org.tutorbooking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.RejectTutorRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.service.TutorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/tutors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class AdminController {

    private final TutorService tutorService;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<TutorDetailResponse>>> getPendingTutors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chờ duyệt thành công", tutorService.getPendingTutors(page, size)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveTutor(@PathVariable Long id) {
        tutorService.approveTutor(id);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt hồ sơ gia sư thành công!"));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTutor(
            @PathVariable Long id,
            @Valid @RequestBody RejectTutorRequest request) {
        tutorService.rejectTutor(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối hồ sơ gia sư!"));
    }
}