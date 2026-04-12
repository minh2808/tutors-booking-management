package org.tutorbooking.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.tutorbooking.dto.response.PaymentLinkResponse;

public interface PaymentService {
    PaymentLinkResponse createPaymentLink(Long userId, Long paymentId) throws Exception;
    void handlePayOSWebhook(JsonNode webhookBody) throws Exception;
}
