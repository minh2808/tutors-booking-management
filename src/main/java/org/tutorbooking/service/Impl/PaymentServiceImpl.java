package org.tutorbooking.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tutorbooking.domain.entity.Payment;
import org.tutorbooking.domain.enums.BookingStatus;
import org.tutorbooking.domain.enums.PaymentStatus;
import org.tutorbooking.dto.response.PaymentLinkResponse;
import org.tutorbooking.exception.ResourceNotFoundException;
import org.tutorbooking.repository.PaymentRepository;
import org.tutorbooking.service.BookingService;
import org.tutorbooking.service.PaymentService;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.WebhookData;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PayOS payOS;
    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public PaymentLinkResponse createPaymentLink(Long userId, Long paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Hóa đơn thanh toán"));

        if (!payment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bạn không có quyền thanh toán Hóa đơn này");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Hóa đơn này đã được thanh toán rồi");
        }

        String itemName = payment.getPaymentType().name().contains("FINDING") ? "Phí Môi Giới Phụ Huynh" : "Phí Nhận Lớp Gia Sư";

        ItemData item = ItemData.builder()
                .name(itemName)
                .quantity(1)
                .price(payment.getAmount().intValue())
                .build();

        // returnUrl va cancelUrl (dành cho frontend xử lý redirect)
        String returnUrl = "http://localhost:3000/payment/success";
        String cancelUrl = "http://localhost:3000/payment/cancel";

        // Generate OrderCode: Sử dụng ID của Payment để PayOS dễ bề cập nhật lại
        long orderCode = payment.getId();

        PaymentData paymentData = PaymentData.builder()
                .orderCode(orderCode)
                .amount(payment.getAmount().intValue())
                .description("Thanh toan HD " + payment.getId())
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);

        return PaymentLinkResponse.builder()
                .paymentId(payment.getId())
                .checkoutUrl(data.getCheckoutUrl())
                .qrCode(data.getQrCode())
                .build();
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(JsonNode webhookBody) throws Exception {
        // Parse the webhook using PayOS SDK (The SDK validates checkSum implicitly if using generic Object Node)
        // Here we do a simplified custom parse based on expected WebhookData
        vn.payos.type.Webhook webhook = objectMapper.treeToValue(webhookBody, vn.payos.type.Webhook.class);
        WebhookData data = payOS.verifyPaymentWebhookData(webhook);

        if ("00".equals(data.getCode()) || "PAID".equalsIgnoreCase(data.getCode())) {
            Long paymentId = data.getOrderCode();
            Payment payment = paymentRepository.findById(paymentId).orElse(null);

            if (payment != null && payment.getStatus() != PaymentStatus.COMPLETED) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(data.getTransactionDateTime());
                paymentRepository.save(payment);


            }
        }
    }
}
