package com.example.emailservice.controller;

import com.example.emailservice.KafkaProducer.KafkaMessagePubliser;
import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("email")
public class EmailController {

    private final EmailService emailService;
    private final KafkaMessagePubliser kafkaMessagePubliser;

    public EmailController(EmailService emailService, KafkaMessagePubliser kafkaMessagePubliser) {
        this.emailService = emailService;
        this.kafkaMessagePubliser = kafkaMessagePubliser;
    }

   /* @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDto emailDto, @RequestPart(value = "attachments", required=false) List<MultipartFile> attachments ) {
        try {
            if(attachments != null && !attachments.isEmpty()) {
                emailDto.setAttachments(attachments);
            }

            emailService.sendEmail(emailDto);
            return ResponseEntity.status(HttpStatus.OK).body("Email sent successfully");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email: " + e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }*/

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDto emailDto, @RequestPart(value = "attachments", required=false) List<MultipartFile> attachments ) {
        try {
            if(attachments != null && !attachments.isEmpty()) {
                emailDto.setAttachments(attachments);
            }

            kafkaMessagePubliser.sendMessage(emailDto,0);
            return ResponseEntity.status(HttpStatus.OK).body("Email sent successfully");
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }


}
