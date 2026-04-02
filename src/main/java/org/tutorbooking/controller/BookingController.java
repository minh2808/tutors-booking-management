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
import org.tutorbooking.service.BookingService;

import java.util.List;

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


    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<BookingResponse> bookings = bookingService.getMyBookings(userPrincipal.getId(), userPrincipal.getRole());
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }


    @PreAuthorize("hasAnyRole('PARENT', 'TUTOR', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {

        BookingResponse booking = bookingService.getBookingById(userPrincipal.getId(), userPrincipal.getRole(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", booking));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {

        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved successfully", bookings));
    }
}
