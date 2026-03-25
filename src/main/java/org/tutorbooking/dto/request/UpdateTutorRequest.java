package org.tutorbooking.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTutorRequest {

    @NotBlank(message = "Education level không được để trống")
    private String educationLevel;

    @Size(max = 2000, message = "Experience quá dài")
    private String experience;

    @Size(max = 2000, message = "Qualifications quá dài")
    private String qualifications;

    @NotBlank(message = "Teaching mode không được để trống")
    private String teachingMode;

    @Size(max = 255, message = "Teaching area quá dài")
    private String teachingArea;
}