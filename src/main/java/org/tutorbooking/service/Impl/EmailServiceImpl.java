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
}
