package org.tutorbooking.service;

public interface EmailService {
    void sendSessionConfirmedEmail(String toEmail, String parentName, String tutorName,
                                    String subjectName, String sessionDate,
                                    String startTime, String endTime);
}
