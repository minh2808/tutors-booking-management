-- Chuyển đổi toàn bộ ENUM của payments sang CẤP ĐỘ VIẾT HOA (UPPERCASE)
-- Để đồng bộ hóa chuẩn phong cách Code Java Hibernate Enum

ALTER TABLE payments
    MODIFY COLUMN payment_type ENUM('CLASS_FINDING_FEE', 'CLASS_RECEIVING_FEE') NOT NULL;

ALTER TABLE payments
    MODIFY COLUMN payment_method ENUM('VNPAY', 'MOMO', 'ZALOPAY', 'BANK_TRANSFER') NULL;

ALTER TABLE payments
    MODIFY COLUMN status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING';
