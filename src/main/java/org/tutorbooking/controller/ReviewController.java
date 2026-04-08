package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.ReviewCreateRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.dto.response.ReviewResponse;
import org.tutorbooking.security.UserPrincipal;
import org.tutorbooking.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewCreateRequest request) {

        ReviewResponse review = reviewService.createReview(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Review created successfully", review));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviews(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ReviewResponse> reviews = reviewService.getReviews(tutorId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
}
