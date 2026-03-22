package org.tutorbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse {
    private boolean success;
    private String message;
<<<<<<< HEAD
    private Object data;
=======
>>>>>>> main
}
