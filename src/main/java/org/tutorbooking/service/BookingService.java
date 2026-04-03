package org.tutorbooking.service;

import org.tutorbooking.dto.request.BookingCreateRequest;
import org.tutorbooking.dto.response.BookingResponse;
import org.tutorbooking.dto.response.PageResponse;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingCreateRequest request);
    PageResponse<BookingResponse> getBookings(Long userId, String role, int page, int size);
    BookingResponse getBookingById(Long userId, String role, Long bookingId);
    BookingResponse pauseBooking(Long userId, Long bookingId);
    BookingResponse resumeBooking(Long userId, Long bookingId);
    BookingResponse cancelBooking(Long userId, String role, Long bookingId);
}

