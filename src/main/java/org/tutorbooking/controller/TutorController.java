package org.tutorbooking.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import org.tutorbooking.domain.entity.Tutor;
import org.tutorbooking.domain.entity.TutorSubject;
import org.tutorbooking.domain.entity.User;
import org.tutorbooking.dto.request.UpdateTutorRequest;
import org.tutorbooking.dto.request.SubjectRequest;
import org.tutorbooking.dto.request.UpdateProfileRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.AuthResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.UserProfileResponse;
import org.tutorbooking.repository.UserRepository;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
    public TutorDetailResponse getTutorDetail(@PathVariable Long id) {
        return tutorService.getTutorDetail(id);
    }
    // @GetMapping("/{id}")
    // public ResponseEntity<?> getTutorDetail(@PathVariable Long id) {
    //     return ResponseEntity.ok(tutorService.getTutorDetail(id));
    // }

    // =========================================
    // 2. TUTOR TỰ XEM PROFILE CỦA MÌNH
    // =========================================
    // @GetMapping("/me")
    // public Tutor getMyProfile(@AuthenticationPrincipal UserPrincipal user) {
    //     return tutorService.getMyProfile(user.getId());
    // }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Tutor>> getMyProfile(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResponse.success("thành công", tutorService.getMyProfile(user.getId()))); // Chạy vèo vèo
    }

    // =========================================
    // 3. UPDATE PROFILE
    // =========================================
    // @PutMapping("/me")
    // public void updateProfile(
    //         @AuthenticationPrincipal UserPrincipal user,
    //         @Valid @RequestBody UpdateTutorRequest req) {
    //     tutorService.updateProfile(user.getId(), req);
    // }
    @PutMapping("/me")
    public void updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateTutorRequest req) {

        // Ép kiểu lấy UserPrincipal ra để lấy ID (Kiểu Long)
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        // Bây giờ truyền principal.getId() là chuẩn kiểu Long, Service sẽ hết báo đỏ
        tutorService.updateProfile(principal.getId(), req);
    }

    // =========================================
    // 4. UPDATE SUBJECTS
    // =========================================
    // @PutMapping("/me/subjects")
    // public void updateSubjects(
    //         @AuthenticationPrincipal UserPrincipal user,
    //         @Valid @RequestBody List<@Valid SubjectRequest> req) {
    //     tutorService.updateSubjects(user.getId(), req);
    // }
    @PutMapping("/me/subjects")
    public void updateSubjects(
            Authentication authentication,
            @Valid @RequestBody List<@Valid SubjectRequest> req) {

        // 1. Lấy email từ Token
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        // 3. Truyền ID vào Service
        tutorService.updateSubjects(principal.getId(), req);
    }

    // =========================================
    // 5. LẤY SUBJECT CỦA 1 TUTOR
    // =========================================
    @GetMapping("/{id}/subjects")
    public List<TutorSubject> getSubjects(@PathVariable Long id) {
        return tutorService.getSubjects(id);
    }
}