package org.tutorbooking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.RejectTutorRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorDetailResponse;
import org.tutorbooking.dto.response.UserAdminResponse;
import org.tutorbooking.dto.response.TopTutorResponse;
import org.tutorbooking.service.TutorService;
import org.tutorbooking.service.UserService;
import org.tutorbooking.domain.enums.Role;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class AdminController {

    private final TutorService tutorService;
    private final UserService userService;

    @GetMapping("/tutors/pending")
    public ResponseEntity<ApiResponse<Page<TutorDetailResponse>>> getPendingTutors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chờ duyệt thành công", tutorService.getPendingTutors(page, size)));
    }

    @PostMapping("/tutors/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveTutor(@PathVariable Long id) {
        tutorService.approveTutor(id);
        return ResponseEntity.ok(ApiResponse.success("Đã duyệt hồ sơ gia sư thành công!"));
    }

    @PostMapping("/tutors/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectTutor(
            @PathVariable Long id,
            @Valid @RequestBody RejectTutorRequest request) {
        tutorService.rejectTutor(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Đã từ chối hồ sơ gia sư!"));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserAdminResponse>>> getAllUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách người dùng thành công", 
                userService.getAllUsersForAdmin(role, isActive, keyword, page, size)
        ));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa tài khoản thành công!"));
    }

    @GetMapping("/dashboard/top-tutors")
    public ResponseEntity<ApiResponse<Page<TopTutorResponse>>> getTopTutors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách top gia sư thành công", 
                tutorService.getTopTutors(page, size)
        ));
    }
}