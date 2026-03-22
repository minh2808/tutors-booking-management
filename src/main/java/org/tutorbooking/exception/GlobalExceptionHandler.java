package org.tutorbooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.mail.MailAuthenticationException;
=======
>>>>>>> UPDATE
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tutorbooking.dto.response.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

<<<<<<< HEAD

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
=======
    // Bắt và xử lý lỗi cú pháp JSON do @Valid ném ra (VD: Thiếu biến, Email xịt...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
>>>>>>> UPDATE
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

<<<<<<< HEAD
        String finalMessage = "Validation failed: " + errors;

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(finalMessage, errors));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("System error: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected internal server error occurred: " + ex.getMessage()));
    }

    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMailAuthenticationException(MailAuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Lỗi cấu hình gửi email: " + ex.getMessage()));
=======
        // Đóng gói mớ lỗi đó thành 1 String
        String finalMessage = "Validation failed: " + errors.toString();

        // Trả về JSON 400 Bad Request kèm thông báo, CHẶN DỨNG việc đá sang trang Google
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(finalMessage)
                        .build());
    }

    // Bắt mẻ lưới mọi lỗi RuntimeException khác chưa lường trước
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("System error: " + ex.getMessage())
                        .build());
    }

    // Cái lưới nằm tận cùng đáy xã hội: Vớt mọi thứ Exception còn lại
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.builder()
                        .success(false)
                        .message("An unexpected internal server error occurred: " + ex.getMessage())
                        .build());
>>>>>>> UPDATE
    }
}
