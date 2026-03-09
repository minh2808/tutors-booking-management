USE tutor_booking;

ALTER TABLE users
    RENAME COLUMN password_hash TO password;

ALTER TABLE users
    ADD COLUMN auth_provider ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL' AFTER role;

ALTER TABLE users
    ADD COLUMN refresh_token VARCHAR(500) NULL AFTER is_active;
