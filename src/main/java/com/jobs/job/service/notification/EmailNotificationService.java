package com.jobs.job.service.notification;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public void sendJobAlert(
            String toEmail,
            String jobTitle,
            String company,
            String source
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("New Job Opportunity: " + jobTitle);
        message.setText(
                "A new job matching your preferences has been found.\n\n" +
                        "Role: " + jobTitle + "\n" +
                        "Company: " + company + "\n" +
                        "Source: " + source + "\n\n" +
                        "Login to your dashboard to view details."
        );

        mailSender.send(message);
    }
}

