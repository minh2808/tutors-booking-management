-- Mở rộng Trạng thái Booking cho luồng Double-Handshake Payment
ALTER TABLE bookings
    MODIFY COLUMN status ENUM('WAITING_TUTOR_CONFIRM', 'PENDING_PAYMENTS', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED') DEFAULT 'WAITING_TUTOR_CONFIRM';

-- Đảm bảo payment_method có bank_transfer để dùng cho PayOS
-- Trong V0803 payment_method ENUM('vnpay', 'momo', 'zalopay', 'bank_transfer') NULL
-- Không cần sửa ENUM của payment_method vì ta map với bank_transfer rồi!
