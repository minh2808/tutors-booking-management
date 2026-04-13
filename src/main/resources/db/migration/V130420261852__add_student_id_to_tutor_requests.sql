ALTER TABLE tutor_requests ADD COLUMN student_id BIGINT;
ALTER TABLE tutor_requests ADD CONSTRAINT fk_tutor_requests_student FOREIGN KEY (student_id) REFERENCES students(id);
