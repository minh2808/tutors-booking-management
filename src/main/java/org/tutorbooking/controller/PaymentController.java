package org.tutorbooking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.tutorbooking.dto.response.PaymentLinkResponse;
import org.tutorbooking.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.tutorbooking.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Các API liên quan đến Cổng Thanh toán VietQR PayOS")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Lấy link thanh toán PayOS (QR Code)")
    @GetMapping("/{id}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentLinkResponse> getCheckoutLink(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) throws Exception {
        return ResponseEntity.ok(paymentService.createPaymentLink(userPrincipal.getId(), id));
    }

    @Operation(summary = "Webhook đón lõng tín hiệu từ PayOS (Không cần Token)")
    @PostMapping("/payos-webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody JsonNode webhookBody) {
        try {
            paymentService.handlePayOSWebhook(webhookBody);
            // PayOS yêu cầu trả về Object json {"success":true} hoặc String
            return ResponseEntity.ok("{\"success\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"success\":false}");
        }
    }
}
