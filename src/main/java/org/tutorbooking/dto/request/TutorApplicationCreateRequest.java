package org.tutorbooking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorApplicationCreateRequest {

    @Positive(message = "Proposed price phải > 0")
    private BigDecimal proposedPrice;

    @Size(max = 2000, message = "Cover letter quá dài")
    private String coverLetter;
}
