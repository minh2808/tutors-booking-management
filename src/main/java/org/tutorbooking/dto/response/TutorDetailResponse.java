package org.tutorbooking.dto.response;

import lombok.Data;

@Data
public class TutorDetailResponse {

    private Long id;

    // thông tin user (chỉ lấy cần thiết)
    private String fullName;
    private String avatarUrl;
    private String email;

    // thông tin tutor
    private String educationLevel;
    private String experience;
    private String qualifications;
    private String teachingMode;
    private String teachingArea;
    private String approvalStatus;
}