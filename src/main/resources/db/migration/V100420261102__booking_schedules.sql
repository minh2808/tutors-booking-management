CREATE TABLE booking_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    day_of_week INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Hút dữ liệu từ bảng Bookings cũ sang bảng BookingSchedules mới để bảo toàn Data
INSERT INTO booking_schedules (booking_id, day_of_week, start_time, end_time)
SELECT id, day_of_week, start_time, end_time 
FROM bookings 
WHERE day_of_week IS NOT NULL AND start_time IS NOT NULL AND end_time IS NOT NULL;

-- Xóa sổ 3 cột rườm rà ở bảng Bookings Cũ sau khi vắt xong sữa
ALTER TABLE bookings
    DROP COLUMN day_of_week,
    DROP COLUMN start_time,
    DROP COLUMN end_time;
