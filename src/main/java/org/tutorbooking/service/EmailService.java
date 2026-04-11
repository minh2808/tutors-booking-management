package org.tutorbooking.service;

import java.math.BigDecimal;

public interface EmailService {
    void sendSessionConfirmedEmail(String toEmail, String parentName, String tutorName,
                                    String subjectName, String sessionDate,
                                    String startTime, String endTime);

    void sendApplicationNotificationEmail(String toEmail, String parentName, String tutorName, 
                                          String subjectName, BigDecimal proposedPrice);
}
