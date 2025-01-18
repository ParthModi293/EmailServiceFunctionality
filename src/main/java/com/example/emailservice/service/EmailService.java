package com.example.emailservice.service;


import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.dto.EmailHistory;
import com.example.emailservice.repository.EmailHistoryRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailHistoryRepository emailHistoryRepository;


    @Value("${spring.mail.username}")
    private String sender;




    public EmailService(JavaMailSender mailSender, EmailHistoryRepository emailHistoryRepository) {
        this.mailSender = mailSender;
        this.emailHistoryRepository = emailHistoryRepository;
    }


    @Async
    public void sendEmail(EmailDto emailDto) throws MessagingException {

        EmailHistory emailHistory = new EmailHistory();

        if (emailDto.getFrom() != null && !emailDto.getFrom().isEmpty()) {
            emailHistory.setFromAddress(emailDto.getFrom());
        }else{
          emailHistory.setFromAddress(sender);
        }

        emailHistory.setToAddresses(String.join(",", emailDto.getTo()));
        emailHistory.setCcAddresses(emailDto.getCc() != null ? String.join(",", emailDto.getCc()) : null);
        emailHistory.setBccAddresses(emailDto.getBcc() != null ? String.join(",", emailDto.getBcc()) : null);
        emailHistory.setSubject(emailDto.getSubject());
        emailHistory.setBody(emailDto.getBody());
        emailHistory.setVersion(emailDto.getVersion());
        emailHistory.setTimestamp(LocalDateTime.now());

        if (emailDto.getAttachments() != null) {
            String attachments = emailDto.getAttachments().stream()
                    .map(MultipartFile::getOriginalFilename)
                    .collect(Collectors.joining(","));
            emailHistory.setAttachments(attachments);
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            if (emailDto.getFrom() != null && !emailDto.getFrom().isEmpty()) {
                helper.setFrom(emailDto.getFrom());
            } else {
                helper.setFrom(sender);
            }

            helper.setTo(emailDto.getTo().toArray(new String[0]));
            helper.setSubject(replacePlaceholders(emailDto.getSubject(), emailDto.getPlaceholders()));
            helper.setText(replacePlaceholders(emailDto.getBody(), emailDto.getPlaceholders()), true);

            if (emailDto.getCc() != null && !emailDto.getCc().isEmpty()) {
                helper.setCc(emailDto.getCc().toArray(new String[0]));
            }

            if (emailDto.getBcc() != null && !emailDto.getBcc().isEmpty()) {
                helper.setBcc(emailDto.getBcc().toArray(new String[0]));
            }

            if (emailDto.getAttachments() != null && !emailDto.getAttachments().isEmpty()) {
                for (MultipartFile file : emailDto.getAttachments()) {
                    helper.addAttachment(Objects.requireNonNull(file.getOriginalFilename()), file);
                }
            }
            mailSender.send(message);
            emailHistory.setStatus("SUCCESS");



        } catch (MessagingException e) {
            e.printStackTrace();
            emailHistory.setStatus("FAILED");
            emailHistory.setErrorMessage(e.getMessage());
        }


        emailHistoryRepository.save(emailHistory);

    }

    private String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null) {
            return text;
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "##" + entry.getKey() + "##";
            text = text.replace(placeholder, entry.getValue());
        }
        return text;
    }
}
