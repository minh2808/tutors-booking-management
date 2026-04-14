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
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.WebhookData;
import vn.payos.model.webhooks.Webhook;

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

        // Nếu Hóa đơn đã từng tạo Link thanh toán rồi, thì móc trong DB ra dùng luôn để tránh lỗi 
        // "Đơn thanh toán đã tồn tại" từ PayOS.
        if (payment.getCheckoutUrl() != null && !payment.getCheckoutUrl().isEmpty()) {
            return PaymentLinkResponse.builder()
                    .paymentId(payment.getId())
                    .checkoutUrl(payment.getCheckoutUrl())
                    .qrCode(payment.getQrCode())
                    .build();
        }

        String itemName = payment.getPaymentType().name().contains("FINDING") ? "Phí Môi Giới Phụ Huynh" : "Phí Nhận Lớp Gia Sư";

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name(itemName)
                .quantity(1)
                .price(payment.getAmount().longValue())
                .build();

        // returnUrl va cancelUrl (dành cho frontend xử lý redirect)
        String returnUrl = "https://tutors-booking-management-fe.vercel.app/payment/success";
        String cancelUrl = "https://tutors-booking-management-fe.vercel.app/payment/cancel";

        // Generate OrderCode: Sử dụng ID của Payment để PayOS dễ bề cập nhật lại
        long orderCode = payment.getId();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(payment.getAmount().longValue())
                .description("Thanh toan HD " + payment.getId())
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .item(item)
                .build();

        CreatePaymentLinkResponse data = payOS.paymentRequests().create(paymentData);

        // Lưu thông tin URL trả về vào DB để cache lại cho các lần fetch sau
        payment.setCheckoutUrl(data.getCheckoutUrl());
        payment.setQrCode(data.getQrCode());
        paymentRepository.save(payment);

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
        Webhook webhook = objectMapper.treeToValue(webhookBody, Webhook.class);
        WebhookData data = payOS.webhooks().verify(webhook);

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
