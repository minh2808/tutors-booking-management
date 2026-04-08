# Changelog

## [2026-04-08]
### Added
- Thêm logic kiểm tra `approval_status = 'approved'` khi xem chi tiết gia sư.
- Thêm phân quyền `@PreAuthorize("hasRole('TUTOR')")` cho các endpoint cập nhật hồ sơ.
- Thêm mới `UserProfileController` đáp ứng chuẩn spec 3.2.1 với các endpoint: `GET /api/auth/me`, `PUT /api/auth/profile`, `POST /api/auth/avatar`.
- Triển khai chức năng upload ảnh đại diện (avatar), lưu giữ dưới dạng chuỗi Data URI Base64.

### Changed
- **Refactor mã nguồn quản lý hồ sơ gia sư:**
  - `TutorController`: Rút gọn chỉ còn 2 endpoint theo spec UI 3.2.2 (`GET /id` và `PUT /profile`).
  - `TutorService`: Loại bỏ các phương thức không sử dụng (`getMyProfile`, `updateSubjects`, `getSubjects`).
  - Thống nhất sử dụng `ResourceNotFoundException` cho các lỗi tìm kiếm thực thể.
- Đổi HTTP Method của API Change Password (`/api/auth/change-password`) từ `POST` sang `PUT`.

### Fixed
- Dọn dẹp dead code và các import không sử dụng trong Repository và Service liên quan đến Tutor.
