package org.tutorbooking;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TutorBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorBookingApplication.class, args);
    }
}
