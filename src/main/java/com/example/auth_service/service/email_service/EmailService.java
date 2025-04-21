package com.example.auth_service.service.email_service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("longbui20021008@gmail.com");

            mailSender.send(message);
            System.out.println(" Gửi email thành công đến: " + to);
        } catch (Exception e) {

            e.printStackTrace(); // In toàn bộ stack trace để debug kỹ hơn
        }
    }
}
