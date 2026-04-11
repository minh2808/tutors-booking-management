package org.tutorbooking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/my-profile")
    public ResponseEntity<ApiResponse<TutorDetailResponse>> getMyTutorProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity
                .ok(ApiResponse.success("Lấy hồ sơ cá nhân thành công", tutorService.getMyTutorProfile(principal.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorDetailResponse>> getTutorDetail(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success("Lấy thông tin chi tiết gia sư thành công", tutorService.getTutorDetail(id)));
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateTutorRequest req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        tutorService.updateProfile(principal.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ chuyên môn thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TutorDetailResponse>>> searchTutors(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String teachingMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TutorDetailResponse> result = tutorService.searchTutors(subjectId, grade, minPrice, maxPrice, teachingMode,
                page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách gia sư thành công", result));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<TutorReviewSummaryResponse>> getTutorReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        TutorReviewSummaryResponse result = tutorService.getTutorReviews(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công", result));
    }
}