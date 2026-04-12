package org.tutorbooking.service.Impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.tutorbooking.service.EmailService;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async
    @Override
    public void sendSessionConfirmedEmail(String toEmail, String parentName, String tutorName,
                                           String subjectName, String sessionDate,
                                           String startTime, String endTime) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "GIASUPRO");
            helper.setTo(toEmail);
            helper.setSubject("Buổi học đã được xác nhận - GIASUPRO");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<h2 style=\"color: #16a34a;\">Xin chào " + parentName + ",</h2>"
                    + "<p>Gia sư <b>" + tutorName + "</b> đã <b style=\"color: #16a34a;\">xác nhận</b> buổi học sau:</p>"
                    + "<table style=\"border-collapse: collapse; width: 100%; max-width: 500px; margin: 20px 0;\">"
                    + "<tr style=\"background-color: #f0fdf4;\">"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Môn học</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + subjectName + "</td></tr>"
                    + "<tr><td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Ngày học</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + sessionDate + "</td></tr>"
                    + "<tr style=\"background-color: #f0fdf4;\">"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Thời gian</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + startTime + " - " + endTime + "</td></tr>"
                    + "<tr><td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Gia sư</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + tutorName + "</td></tr>"
                    + "</table>"
                    + "<p>Vui lòng chuẩn bị cho buổi học đúng giờ nhé!</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 30px;\" />"
                    + "<p style=\"font-size: 12px; color: #999;\">Trân trọng,<br>Đội ngũ hỗ trợ GIASUPRO</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Confirmation email sent to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send confirmation email to {}: ", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendApplicationNotificationEmail(String toEmail, String parentName, String tutorName, String subjectName, BigDecimal proposedPrice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "GIASUPRO");
            helper.setTo(toEmail);
            helper.setSubject("Tin Vui - Có Gia sư mới nộp hồ sơ xin dạy môn " + subjectName);

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<h2 style=\"color: #0284c7;\">Xin chào " + parentName + ",</h2>"
                    + "<p>Gia sư <b>" + tutorName + "</b> vừa mới nộp hồ sơ xin nhận lớp của bạn.</p>"
                    + "<table style=\"border-collapse: collapse; width: 100%; max-width: 500px; margin: 20px 0;\">"
                    + "<tr style=\"background-color: #f0f9ff;\">"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Môn học</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + subjectName + "</td></tr>"
                    + "<tr><td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Mức lương đề xuất</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; color: #ea580c; font-weight: bold;\">" + proposedPrice + " VNĐ/Buổi</td></tr>"
                    + "</table>"
                    + "<p>Hãy đăng nhập ngay vào hệ thống để xem chi tiết Profile của gia sư và đưa ra quyết định nhé!</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 30px;\" />"
                    + "<p style=\"font-size: 12px; color: #999;\">Trân trọng,<br>Đội ngũ hỗ trợ GIASUPRO</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Application notification email sent to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send application notification email to {}: ", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendSessionCancelledEmail(String toEmail, String receiverName, String cancellerRoleName,
                                          String subjectName, String sessionDate,
                                          String startTime, String endTime, String cancelReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "GIASUPRO");
            helper.setTo(toEmail);
            helper.setSubject("THÔNG BÁO HỦY LỊCH HỌC ĐỘT XUẤT - GIASUPRO");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<h2 style=\"color: #ef4444;\">Xin chào " + receiverName + ",</h2>"
                    + "<p><b style=\"color: #ef4444;\">" + cancellerRoleName + "</b> vừa thông báo <b style=\"color: #ef4444;\">HỦY</b> buổi học với chi tiết như sau:</p>"
                    + "<table style=\"border-collapse: collapse; width: 100%; max-width: 500px; margin: 20px 0;\">"
                    + "<tr style=\"background-color: #fef2f2;\">"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Môn học</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + subjectName + "</td></tr>"
                    + "<tr><td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Ngày học</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + sessionDate + "</td></tr>"
                    + "<tr style=\"background-color: #fef2f2;\">"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold;\">Thời gian</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\">" + startTime + " - " + endTime + "</td></tr>"
                    + "<tr><td style=\"padding: 10px; border: 1px solid #ddd; font-weight: bold; color: #ef4444;\">Lý do hủy</td>"
                    + "<td style=\"padding: 10px; border: 1px solid #ddd;\"><i>" + cancelReason + "</i></td></tr>"
                    + "</table>"
                    + "<p>Xin vui lòng cập nhật lại lịch trình của bạn để không ảnh hưởng đến công việc nhé!</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 30px;\" />"
                    + "<p style=\"font-size: 12px; color: #999;\">Trân trọng,<br>Đội ngũ hỗ trợ GIASUPRO</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Cancellation email sent to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send cancellation email to {}: ", toEmail, e);
        }
    }

    @Async
    @Override
    public void sendBookingStatusChangedEmail(String toEmail, String receiverName, String subjectName, String newStatus) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail, "GIASUPRO");
            helper.setTo(toEmail);
            
            String statusText = "";
            String color = "";
            if ("PAUSED".equals(newStatus)) {
                statusText = "TẠM DỪNG";
                color = "#d97706"; // orange
            } else if ("CANCELLED".equals(newStatus)) {
                statusText = "ĐÃ BỊ HỦY CHẤM DỨT";
                color = "#ef4444"; // red
            } else if ("ACTIVE".equals(newStatus)) {
                statusText = "MỞ LẠI DẠY TIẾP";
                color = "#16a34a"; // green
            }

            helper.setSubject("CẬP NHẬT TRẠNG THÁI KHÓA HỌC: " + statusText);

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">"
                    + "<h2 style=\"color: " + color + ";\">Xin chào " + receiverName + ",</h2>"
                    + "<p>Khóa học môn <b>" + subjectName + "</b> của bạn vừa mới được đối tác chuyển sang trạng thái: <b style=\"color: " + color + ";\">" + statusText + "</b>.</p>"
                    + "<p>Xin vui lòng đăng nhập vào hệ thống để kiểm tra lịch học bị ảnh hưởng nhé!</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #eee; margin-top: 30px;\" />"
                    + "<p style=\"font-size: 12px; color: #999;\">Trân trọng,<br>Đội ngũ hỗ trợ GIASUPRO</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Booking status email sent to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send booking status email to {}: ", toEmail, e);
        }
    }
}
