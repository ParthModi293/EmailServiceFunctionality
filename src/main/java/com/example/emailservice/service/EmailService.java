package com.example.emailservice.service;

import com.example.emailservice.bean.DynamicMailSender;
import com.example.emailservice.bean.EmailBean;
import com.example.emailservice.common.ResponseBean;
import com.example.emailservice.dto.EmailConfiguration;
import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.dto.EmailHistory;
import com.example.emailservice.dto.EmailPropertiesDto;
import com.example.emailservice.repository.EmailConfigurationRepository;
import com.example.emailservice.repository.EmailDao;
import com.example.emailservice.repository.EmailHistoryRepository;
import com.example.emailservice.util.JacksonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Log4j2
public class EmailService {


    private final EmailHistoryRepository emailHistoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EmailDao emailDao;
    private final ObjectMapper objectMapper;


    private final DynamicMailSender dynamicMailSender;

 /*   @Value("${spring.mail.username}")
    private String sender;*/


    public EmailService(EmailHistoryRepository emailHistoryRepository, KafkaTemplate<String, Object> kafkaTemplate, EmailDao emailDao, ObjectMapper objectMapper, EmailConfigurationRepository emailConfigurationRepository, DynamicMailSender dynamicMailSender) {
        this.emailHistoryRepository = emailHistoryRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.emailDao = emailDao;
        this.objectMapper = objectMapper;
        this.dynamicMailSender = dynamicMailSender;
    }

    public void sendEmail(EmailDto emailDto) throws MessagingException {


//        EmailConfiguration emailConfiguration = emailConfigurationRepository.findByUsername(emailDto.getFrom());


        EmailPropertiesDto emailProperties = EmailPropertiesDto.builder()
                .username(emailDto.getFrom())
                .password(emailDto.getPassword())
                .host(emailDto.getHost())
                .port(emailDto.getPort()).build();

        JavaMailSender mailSender = dynamicMailSender.createMailSender(emailProperties);

        EmailHistory emailHistory = new EmailHistory();

        emailHistory.setFromAddress(emailProperties.getUsername());
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

            helper.setFrom(emailProperties.getUsername());
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
            System.out.println("Here Start----->>>>>>"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

            mailSender.send(message);
            System.out.println("Here End----->>>>>>"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

            emailHistory.setStatus("SUCCESS");


        } catch (Exception e) {
            log.error(e);
            emailHistory.setStatus("FAILED");
            emailHistory.setErrorMessage(e.getMessage());
        } finally {
            emailHistoryRepository.save(emailHistory);

        }


    }

    public ResponseBean<Void> mailSender(EmailBean emailBean) throws Exception {


        Map mailDetails = emailDao.getMailDetails(emailBean.getTemplateName(), emailBean.getDbName());
        if (mailDetails == null || mailDetails.isEmpty()) {
            mailDetails = emailDao.getMailDetails(emailBean.getTemplateName(), "email");
        }

        String body = mailDetails.get("body").toString();
        String subject = mailDetails.get("subject").toString();

        for (Map.Entry<String, Object> entry : emailBean.getBodyPlaceHolder().entrySet()) {
            String placeholder = "##" + entry.getKey() + "##";
            body = body.replace(placeholder, entry.getValue().toString());

        }

        for (Map.Entry<String, Object> entry : emailBean.getSubjectPlaceHolder().entrySet()) {
            String placeholder = "##" + entry.getKey() + "##";

            subject = subject.replace(placeholder, entry.getValue().toString());
        }

        EmailDto emailDto = EmailDto.builder()
                .from(mailDetails.get("fromEmailId").toString())
                .to(emailBean.getTo())
                .cc(emailBean.getCc())
                .bcc(emailBean.getBcc())
                .subject(subject)
                .body(body)
                .version(mailDetails.get("version").toString())
                .attachments(emailBean.getFile())
                .host(mailDetails.get("host").toString())
                .password(mailDetails.get("password").toString())
                .port(Integer.parseInt(mailDetails.get("port").toString()))
                .build();

        kafkaTemplate.send("email-topic", Integer.parseInt(mailDetails.get("priority").toString()), null, new ObjectMapper().writeValueAsString(emailDto));
        return new ResponseBean<>(HttpStatus.OK, "Mail sender under process", "Mail sender under process", null);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"1"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public void emailConsumer1P1(String message) {
        try {
            EmailDto emailDto = objectMapper.readValue(message, EmailDto.class);

                    sendEmail(emailDto);
//                    System.out.println("1-------------------------------------------------------------1");
        } catch (Exception e) {
            log.error(e);
        }
    }
  /*  @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"1"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public void emailConsumer2P1(String message) {
        try {
            EmailDto emailDto = objectMapper.readValue(message, EmailDto.class);
            sendEmail(emailDto);
            System.out.println("2 *********************************************************** 2");
        } catch (Exception e) {
            log.error(e);
        }
    }*/
    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"2"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public void emailConsumer2(String message) {
        try {
            EmailDto emailDto = objectMapper.readValue(message, EmailDto.class);
            sendEmail(emailDto);

            System.out.println("Mail sent-------------------------------------->>>> " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        } catch (Exception e) {
            log.error(e);

        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "email-topic", partitions = {"3"}), groupId = "email-group", errorHandler = "myErrorHandler")
    public void emailConsumer3(String message) {
        try {
            EmailDto emailDto = objectMapper.readValue(message, EmailDto.class);
            sendEmail(emailDto);

            System.out.println("Mail sent-------------------------------------->>>> " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        } catch (Exception e) {
            log.error(e);

        }
    }

}
