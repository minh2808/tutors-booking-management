package org.tutorbooking.service;

import java.math.BigDecimal;

public interface EmailService {
    void sendSessionConfirmedEmail(String toEmail, String parentName, String tutorName,
                                    String subjectName, String sessionDate,
                                    String startTime, String endTime);

    void sendApplicationNotificationEmail(String toEmail, String parentName, String tutorName, 
                                          String subjectName, BigDecimal proposedPrice);

    void sendSessionCancelledEmail(String toEmail, String receiverName, String cancellerRoleName,
                                   String subjectName, String sessionDate,
                                   String startTime, String endTime, String cancelReason);

    void sendBookingStatusChangedEmail(String toEmail, String receiverName, String subjectName, String newStatus);
}
