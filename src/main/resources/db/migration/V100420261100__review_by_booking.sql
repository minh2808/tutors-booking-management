-- Đổi reviews từ session-based sang booking-based
ALTER TABLE reviews DROP FOREIGN KEY reviews_ibfk_1;
ALTER TABLE reviews DROP COLUMN session_id;

ALTER TABLE reviews ADD COLUMN booking_id BIGINT NOT NULL AFTER id;
ALTER TABLE reviews ADD UNIQUE KEY uq_reviews_booking (booking_id);
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;

-- Thêm COMPLETED vào enum booking status
ALTER TABLE bookings
    MODIFY COLUMN status ENUM('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') DEFAULT 'ACTIVE';
