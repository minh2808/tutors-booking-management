package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.TutorAvailabilityRequest;
import org.tutorbooking.dto.request.TutorSubjectRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorAvailabilityResponse;
import org.tutorbooking.dto.response.TutorSubjectResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.TutorPortfolioService;

import java.util.List;

@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorPortfolioController {

    private final TutorPortfolioService portfolioService;

    // --- Cấu hình Môn dạy & Giá ---

    // #9. Xem DS môn + giá theo từng lớp của 1 GS cụ thể (Không Auth)
    @GetMapping("/{id}/subjects")
    public ResponseEntity<ApiResponse<List<TutorSubjectResponse>>> getTutorSubjects(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách môn dạy thành công", portfolioService.getTutorSubjects(id)));
    }

    // #10. GS thêm môn dạy + giá (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<TutorSubjectResponse>> addTutorSubject(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TutorSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm môn dạy thành công", 
                portfolioService.addTutorSubject(principal.getId(), request)));
    }

    // #11. GS sửa giá cho môn/lớp đã đăng ký (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<TutorSubjectResponse>> updateTutorSubject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long subjectId,
            @Valid @RequestBody TutorSubjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật môn dạy thành công", 
                portfolioService.updateTutorSubject(principal.getId(), subjectId, request)));
    }

    // #12. GS xóa 1 môn khỏi danh sách dịch vụ (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> removeTutorSubject(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long subjectId) {
        portfolioService.removeTutorSubject(principal.getId(), subjectId);
        return ResponseEntity.ok(ApiResponse.success("Xóa môn dạy thành công", null));
    }


    // --- Cấu hình Lịch rảnh ---

    // #13. Xem khung giờ rảnh của GS theo ngày trong tuần (Không Auth)
    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<List<TutorAvailabilityResponse>>> getTutorAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch rảnh thành công", portfolioService.getTutorAvailability(id)));
    }

    // #14. GS thêm khung giờ rảnh (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<TutorAvailabilityResponse>> addTutorAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TutorAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Thêm lịch rảnh thành công", 
                portfolioService.addTutorAvailability(principal.getId(), request)));
    }

    // #15. GS sửa khung giờ rảnh đã đăng ký (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<TutorAvailabilityResponse>> updateTutorAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long availabilityId,
            @Valid @RequestBody TutorAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật lịch rảnh thành công", 
                portfolioService.updateTutorAvailability(principal.getId(), availabilityId, request)));
    }

    // #16. GS xóa khung giờ rảnh (Yêu cầu role GS)
    @PreAuthorize("hasRole('TUTOR')")
    @DeleteMapping("/availability/{availabilityId}")
    public ResponseEntity<ApiResponse<Void>> removeTutorAvailability(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long availabilityId) {
        portfolioService.removeTutorAvailability(principal.getId(), availabilityId);
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch rảnh thành công", null));
    }
}
