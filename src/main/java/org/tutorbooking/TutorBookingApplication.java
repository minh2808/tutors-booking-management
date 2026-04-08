package org.tutorbooking;

import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication
// @EnableAsync
@SpringBootApplication(exclude = { OpenAiAutoConfiguration.class })
public class TutorBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorBookingApplication.class, args);
    }
}
