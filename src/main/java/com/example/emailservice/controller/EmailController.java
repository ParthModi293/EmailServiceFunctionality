package com.example.emailservice.controller;

import com.example.emailservice.bean.EmailBean;
import com.example.emailservice.common.ResponseBean;
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
@RequestMapping("/email-service")
@CrossOrigin
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
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

    @PostMapping("/send-email")
    public ResponseEntity<ResponseBean<?>> filterAlert(@RequestBody EmailBean emailBean ) throws Exception {
        ResponseBean<?> responseBean = emailService.mailSender(emailBean);
        return new ResponseEntity<>(responseBean, responseBean.getRStatus());
    }


}
