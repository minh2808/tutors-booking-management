package org.tutorbooking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.TutorReviewSummaryResponse;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.Page;
import java.util.List;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    // =========================================
    // 1. KHÁCH XEM DETAIL CỦA TUTOR
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorDetailResponse>> getTutorDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chi tiết gia sư thành công", tutorService.getTutorDetail(id)));
    }

    // =========================================
    // 2. TUTOR TỰ XEM PROFILE CỦA MÌNH
    // =========================================
    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Tutor>> getMyProfile(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("Lấy hồ sơ cá nhân thành công", tutorService.getMyProfile(user.getId())));
    }

    // =========================================
    // 3. UPDATE PROFILE
    // =========================================
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateTutorRequest req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        tutorService.updateProfile(principal.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật hồ sơ thành công"));
    }

    // =========================================
    // 4. UPDATE SUBJECTS
    // =========================================
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/profile/subjects")
    public ResponseEntity<ApiResponse<Void>> updateSubjects(
            Authentication authentication,
            @Valid @RequestBody List<@Valid SubjectRequest> req) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        tutorService.updateSubjects(principal.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn học thành công"));
    }

    // =========================================
    // 5. LẤY SUBJECT CỦA 1 TUTOR
    // =========================================
    @GetMapping("/{id}/subjects")
    public ResponseEntity<ApiResponse<List<TutorSubject>>> getSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn học thành công", tutorService.getSubjects(id)));
    }

    // =========================================
    // 6. TÌM KIẾM & LỌC GIA SƯ ĐÃ DUYỆT (PUBLIC)
    // =========================================
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TutorDetailResponse>>> searchTutors(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String teachingMode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<TutorDetailResponse> result = tutorService.searchTutors(subjectId, grade, minPrice, maxPrice, teachingMode, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách gia sư thành công", result));
    }

    // =========================================
    // 7. XEM ĐÁNH GIÁ & SỐ SAO CỦA 1 GIA SƯ (PUBLIC)
    // =========================================
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<TutorReviewSummaryResponse>> getTutorReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        TutorReviewSummaryResponse result = tutorService.getTutorReviews(id, page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy đánh giá thành công", result));
    }
}