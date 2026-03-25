
-- =====================================================
-- 1. USERS — Người dùng
-- =====================================================
CREATE TABLE users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('admin', 'tutor', 'parent') NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    avatar_url    VARCHAR(500),
    is_active     BOOLEAN   DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at    TIMESTAMP NULL
);

-- =====================================================
-- 2. TUTORS — Hồ sơ gia sư
-- =====================================================
CREATE TABLE tutors
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL UNIQUE,
    education_level  ENUM('high_school', 'bachelor', 'master', 'phd', 'other') DEFAULT 'bachelor',
    experience       TEXT,
    qualifications   TEXT,
    teaching_mode    ENUM('online', 'offline', 'both') DEFAULT 'both', -- lỗi thì sửa not null
    teaching_area    VARCHAR(255),
    approval_status  ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    rejection_reason TEXT,
    approved_at      TIMESTAMP NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- =====================================================
-- 3. PARENTS — Hồ sơ phụ huynh
-- =====================================================
CREATE TABLE parents
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE,
    address    VARCHAR(255),
    district   VARCHAR(100),
    city       VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- =====================================================
-- 4. STUDENTS — Thông tin con
-- =====================================================
CREATE TABLE students
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id      BIGINT          NOT NULL,
    full_name      VARCHAR(100) NOT NULL,
    grade          TINYINT      NOT NULL,
    school         VARCHAR(200),
    academic_level ENUM('excellent', 'good', 'average', 'weak') DEFAULT 'average',
    special_notes  TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES parents (id) ON DELETE CASCADE
);

-- =====================================================
-- 5. SUBJECTS — Môn học
-- =====================================================
CREATE TABLE subjects
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =====================================================
-- 6. TUTOR_SUBJECTS — Gia sư dạy môn gì, giá theo lớp
-- =====================================================
CREATE TABLE tutor_subjects
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    tutor_id          BIGINT            NOT NULL,
    subject_id        BIGINT            NOT NULL,
    grade_level       TINYINT        NOT NULL,
    price_per_session DECIMAL(10, 0) NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (tutor_id, subject_id, grade_level),
    FOREIGN KEY (tutor_id) REFERENCES tutors (id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE
);

-- =====================================================
-- 7. TUTOR_AVAILABILITY — Khung giờ rảnh gia sư
-- =====================================================
CREATE TABLE tutor_availability
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    tutor_id    BIGINT     NOT NULL,
    day_of_week TINYINT NOT NULL,
    start_time  TIME    NOT NULL,
    end_time    TIME    NOT NULL,
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tutor_id) REFERENCES tutors (id) ON DELETE CASCADE
);

-- =====================================================
-- 8. TUTOR_REQUESTS — Yêu cầu tìm gia sư
-- =====================================================
CREATE TABLE tutor_requests
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id         BIGINT     NOT NULL,
    subject_id        BIGINT     NOT NULL,
    grade_level       TINYINT NOT NULL,
    desired_price     DECIMAL(10, 0),
    teaching_mode     ENUM('online', 'offline', 'both') DEFAULT 'both',
    preferred_area    VARCHAR(255),
    schedule_note     TEXT,
    sessions_per_week TINYINT   DEFAULT 1,
    status            ENUM('pending', 'searching', 'has_applicants', 'matched', 'cancelled') DEFAULT 'pending',
    approved_at       TIMESTAMP NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES parents (id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT
);

-- =====================================================
-- 9. TUTOR_APPLICATIONS — Gia sư ứng tuyển
-- =====================================================
CREATE TABLE tutor_applications
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id     BIGINT NOT NULL,
    tutor_id       BIGINT NOT NULL,
    proposed_price DECIMAL(10, 0),
    cover_letter   TEXT,
    status         ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
    responded_at   TIMESTAMP NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (request_id, tutor_id),
    FOREIGN KEY (request_id) REFERENCES tutor_requests (id) ON DELETE CASCADE,
    FOREIGN KEY (tutor_id) REFERENCES tutors (id) ON DELETE CASCADE
);

-- =====================================================
-- 10. BOOKINGS — Lịch đặt học
-- =====================================================
CREATE TABLE bookings
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id            BIGINT            NOT NULL,
    tutor_id             BIGINT            NOT NULL,
    subject_id           BIGINT            NOT NULL,
    student_id           BIGINT NULL,
    grade_level          TINYINT        NOT NULL,
    price_per_session    DECIMAL(10, 0) NOT NULL,
    teaching_mode        ENUM('online', 'offline') NOT NULL,
    is_recurring         BOOLEAN   DEFAULT FALSE,
    day_of_week          TINYINT NULL,
    start_time           TIME           NOT NULL,
    end_time             TIME           NOT NULL,
    recurring_start_date DATE NULL,
    recurring_end_date   DATE NULL,
    status               ENUM('active', 'paused', 'cancelled') DEFAULT 'active',
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES parents (id) ON DELETE RESTRICT,
    FOREIGN KEY (tutor_id) REFERENCES tutors (id) ON DELETE RESTRICT,
    FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE SET NULL
);

-- =====================================================
-- 11. SESSIONS — Từng buổi học cụ thể
-- =====================================================
CREATE TABLE sessions
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id    BIGINT  NOT NULL,
    session_date  DATE NOT NULL,
    start_time    TIME NOT NULL,
    end_time      TIME NOT NULL,
    status        ENUM('pending', 'confirmed', 'cancelled', 'completed') DEFAULT 'pending',
    cancelled_by  BIGINT NULL,
    cancel_reason TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    FOREIGN KEY (cancelled_by) REFERENCES users (id) ON DELETE SET NULL
);

-- =====================================================
-- 12. REVIEWS — Đánh giá gia sư
-- =====================================================
CREATE TABLE reviews
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT     NOT NULL UNIQUE,
    parent_id  BIGINT     NOT NULL,
    tutor_id   BIGINT     NOT NULL,
    rating     TINYINT NOT NULL,
    comment    TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions (id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES parents (id) ON DELETE CASCADE,
    FOREIGN KEY (tutor_id) REFERENCES tutors (id) ON DELETE CASCADE
);

-- =====================================================
-- 13. PAYMENTS — Thanh toán phí dịch vụ
-- =====================================================
CREATE TABLE payments
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT            NOT NULL,
    booking_id     BIGINT NULL,
    request_id     BIGINT NULL,
    amount         DECIMAL(12, 0) NOT NULL,
    payment_type   ENUM('class_finding_fee', 'class_receiving_fee') NOT NULL,
    payment_method ENUM('vnpay', 'momo', 'zalopay', 'bank_transfer') NULL,
    status         ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    transaction_id VARCHAR(255),
    paid_at        TIMESTAMP NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL,
    FOREIGN KEY (request_id) REFERENCES tutor_requests (id) ON DELETE SET NULL
);

-- =====================================================
-- 14. NOTIFICATIONS — Thông báo
-- =====================================================
CREATE TABLE notifications
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT          NOT NULL,
    title          VARCHAR(255) NOT NULL,
    content        TEXT,
    type           ENUM(
        'booking_confirmed',
        'booking_cancelled',
        'tutor_approved',
        'tutor_rejected',
        'new_application',
        'application_accepted',
        'application_rejected',
        'payment_due',
        'payment_completed',
        'new_review'
    ) NOT NULL,
    is_read        BOOLEAN   DEFAULT FALSE,
    reference_type VARCHAR(50),
    reference_id   BIGINT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
