package org.tutorbooking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.tutorbooking.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.request.BookingCreateRequest;
import org.tutorbooking.dto.response.ApiResponse;
import org.tutorbooking.dto.response.BookingResponse;
import org.tutorbooking.dto.response.PageResponse;
import org.tutorbooking.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasRole('PARENT')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BookingCreateRequest request) {

        BookingResponse response = bookingService.createBooking(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking and sessions created successfully", response));
    }

  
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<BookingResponse> bookings = bookingService.getBookings(
                userPrincipal.getId(), userPrincipal.getRole(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

  
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        BookingResponse booking = bookingService.getBookingById(userPrincipal.getId(), userPrincipal.getRole(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PutMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<BookingResponse>> pauseBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        BookingResponse response = bookingService.pauseBooking(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking paused successfully", response));
    }

    @PreAuthorize("hasRole('PARENT')")
    @PutMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<BookingResponse>> resumeBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        BookingResponse response = bookingService.resumeBooking(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking resumed successfully", response));
    }

    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        BookingResponse response = bookingService.cancelBooking(userPrincipal.getId(), userPrincipal.getRole(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }
}
