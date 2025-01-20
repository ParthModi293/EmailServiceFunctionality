package com.example.emailservice.service;


import com.example.emailservice.bean.EmailBean;
import com.example.emailservice.common.ResponseBean;
import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.dto.EmailHistory;
import com.example.emailservice.repository.EmailDao;
import com.example.emailservice.repository.EmailHistoryRepository;
import com.example.emailservice.util.JacksonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
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
@Log4j2
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailHistoryRepository emailHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EmailDao emailDao;


    @Value("${spring.mail.username}")
    private String sender;


    public EmailService(JavaMailSender mailSender, EmailHistoryRepository emailHistoryRepository, KafkaTemplate<String, Object> kafkaTemplate, EmailDao emailDao) {
        this.mailSender = mailSender;
        this.emailHistoryRepository = emailHistoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.emailDao = emailDao;
    }


    @Async
    public void sendEmail(EmailDto emailDto) throws MessagingException {

        EmailHistory emailHistory = new EmailHistory();

        if (emailDto.getFrom() != null && !emailDto.getFrom().isEmpty()) {
            emailHistory.setFromAddress(emailDto.getFrom());
        } else {
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
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getBody(), true);

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

    /*private String replacePlaceholders(String text, Map<String, String> placeholders) {
        if (text == null || placeholders == null) {
            return text;
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "##" + entry.getKey() + "##";
            text = text.replace(placeholder, entry.getValue());
        }
        return text;
    }*/

    public ResponseBean<Void> mailSender(EmailBean emailBean, MultipartFile file) throws Exception {
        Map mailDetails = emailDao.getMailDetails(emailBean.getTemplateName());

        String body = mailDetails.get("body").toString();
        String subject = mailDetails.get("subject").toString();

        System.out.println("Raw body:" + body);
        System.out.println("Raw subject:" + subject);

        for (Map.Entry<String, Object> entry : emailBean.getPlaceHolder().entrySet()) {
            String placeholder = "##" + entry.getKey() + "##";
            body = body.replace(placeholder, entry.getValue().toString());
            subject = subject.replace(placeholder, entry.getValue().toString());
        }

        System.out.println("Final body:" + body);
        System.out.println("Final subject:" + subject);

        EmailDto emailDto = EmailDto.builder()
                .from(emailBean.getFrom())
                .to(emailBean.getTo())
                .cc(emailBean.getCc())
                .bcc(emailBean.getBcc())
                .subject(subject)
                .body(body)
                .version(mailDetails.get("version").toString())
                .attachments(emailBean.getFile())
                .build();

        // TODO: set s3 path into the attachment section that can be multiple
        emailDto.getAttachments().add(file);

        kafkaTemplate.send("email-topic", Integer.parseInt(mailDetails.get("priority").toString()), null, new ObjectMapper().writeValueAsString(emailDto));
        return new ResponseBean<>(HttpStatus.OK, "Mail sender under process", "Mail sender under process", null);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"1"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public ResponseBean<Void> emailConsumer1(String message) {
        try {
            EmailDto emailDto = JacksonService.getInstance().convertJsonToDto(message, EmailDto.class);
            sendEmail(emailDto);
            return new ResponseBean<>(HttpStatus.OK, "Main sent", "Main sent", null);
        } catch (Exception e) {
            log.error(e);
            return new ResponseBean<>(HttpStatus.BAD_REQUEST, "Error during mail sent", "Error during mail sent", null);
        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"2"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public ResponseBean<Void> emailConsumer2(String message) {
        try {
            EmailDto emailDto = JacksonService.getInstance().convertJsonToDto(message, EmailDto.class);
            sendEmail(emailDto);
            return new ResponseBean<>(HttpStatus.OK, "Main sent", "Main sent", null);
        } catch (Exception e) {
            log.error(e);
            return new ResponseBean<>(HttpStatus.BAD_REQUEST, "Error during mail sent", "Error during mail sent", null);
        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"3"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public ResponseBean<Void> emailConsumer3(String message) {
        try {
            EmailDto emailDto = JacksonService.getInstance().convertJsonToDto(message, EmailDto.class);
            sendEmail(emailDto);
            return new ResponseBean<>(HttpStatus.OK, "Main sent", "Main sent", null);
        } catch (Exception e) {
            log.error(e);
            return new ResponseBean<>(HttpStatus.BAD_REQUEST, "Error during mail sent", "Error during mail sent", null);
        }
    }
}
