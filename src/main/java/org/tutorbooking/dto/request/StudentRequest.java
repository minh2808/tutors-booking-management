package org.tutorbooking.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentRequest {
    
    @NotBlank(message = "Tên học sinh không được để trống")
    private String fullName;

    @NotNull(message = "Lớp học không được để trống")
    @Min(value = 1, message = "Lớp học nhỏ nhất là lớp 1")
    @Max(value = 12, message = "Lớp học lớn nhất là lớp 12")
    private Byte grade;

    private String school;
    
    // Giỏi/Khá/Trung bình/Yếu
    private String academicLevel;
    
    private String specialNotes;
}
