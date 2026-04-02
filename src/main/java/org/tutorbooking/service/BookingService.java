package org.tutorbooking.service;

import org.tutorbooking.dto.request.BookingCreateRequest;
import org.tutorbooking.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(Long userId, BookingCreateRequest request);
    List<BookingResponse> getMyBookings(Long userId, String role);
    BookingResponse getBookingById(Long userId, String role, Long bookingId);
    List<BookingResponse> getAllBookings();
}
