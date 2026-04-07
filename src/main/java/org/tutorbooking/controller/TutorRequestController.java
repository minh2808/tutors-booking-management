package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.domain.entity.TutorRequest;
import org.tutorbooking.dto.request.TutorApplicationCreateRequest;
import org.tutorbooking.dto.request.TutorRequestCreateRequest;
import org.tutorbooking.dto.request.TutorRequestUpdateRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.TutorApplicationResponse;
import org.tutorbooking.dto.response.TutorRequestResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.TutorRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/tutor-requests")
@RequiredArgsConstructor
public class TutorRequestController {

    private final TutorRequestService tutorRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<TutorRequest>> createRequest(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody TutorRequestCreateRequest req) {
        TutorRequest tutorRequest = tutorRequestService.createRequest(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu thành công", tutorRequest));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<List<TutorRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal user) {
        List<TutorRequestResponse> requests = tutorRequestService.getMyRequests(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu thành công", requests));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TutorRequestResponse>> getRequestDetail(@PathVariable Long id) {
        TutorRequestResponse request = tutorRequestService.getRequestDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết yêu cầu thành công", request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody TutorRequestUpdateRequest req) {
        tutorRequestService.updateRequest(id, user.getId(), req);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật yêu cầu thành công", ""));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal user) {
        tutorRequestService.cancelRequest(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Hủy yêu cầu thành công", ""));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TutorRequestResponse>>> getAllRequests() {
        List<TutorRequestResponse> requests = tutorRequestService.getAllRequests();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu thành công", requests));
    }

    @PostMapping("/{requestId}/apply")
    public ResponseEntity<ApiResponse<String>> applyForRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody TutorApplicationCreateRequest req) {
        tutorRequestService.applyForRequest(requestId, user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ứng tuyển thành công", ""));
    }

    @GetMapping("/applications/my-applications")
    public ResponseEntity<ApiResponse<List<TutorApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserPrincipal user) {
        List<TutorApplicationResponse> applications = tutorRequestService.getMyApplications(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng tuyển thành công", applications));
    }

    @DeleteMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<ApiResponse<String>> withdrawApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        tutorRequestService.withdrawApplication(applicationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Rút lại ứng tuyển thành công", ""));
    }

    @GetMapping("/{requestId}/applications")
    public ResponseEntity<ApiResponse<List<TutorApplicationResponse>>> getApplicationsForRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserPrincipal user) {
        List<TutorApplicationResponse> applications = tutorRequestService.getApplicationsForRequest(requestId,
                user.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ứng tuyển thành công", applications));
    }

    @PostMapping("/applications/{applicationId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        tutorRequestService.acceptApplication(applicationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Chấp nhận ứng tuyển thành công", ""));
    }

    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectApplication(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserPrincipal user) {
        tutorRequestService.rejectApplication(applicationId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Từ chối ứng tuyển thành công", ""));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<TutorApplicationResponse>> getApplicationDetail(
            @PathVariable Long applicationId) {
        TutorApplicationResponse application = tutorRequestService.getApplicationDetail(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết ứng tuyển thành công", application));
    }
}
