package com.quickship.servicenotification.service;
import com.quickship.servicenotification.dto.NotificationEmailDto;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class NotificationService {
    private final JavaMailSender mailSender;
    public NotificationService(JavaMailSender mailSender) { this.mailSender = mailSender; }
    public void envoyerEmail(NotificationEmailDto dto) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@quickship.com");
        message.setTo(dto.destinataire());
        message.setSubject(dto.objet());
        message.setText(dto.contenu());
        mailSender.send(message);
    }
}
