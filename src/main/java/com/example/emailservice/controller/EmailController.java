package com.example.emailservice.controller;

import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public String sendEmail(@RequestBody EmailDto emailDto, @RequestPart(value = "attachments", required=false) List<MultipartFile> attachments ) {
        try {
            if(attachments != null && !attachments.isEmpty()) {
                emailDto.setAttachments(attachments);
            }

            emailService.sendEmail(emailDto);
            return "Email sent successfully!";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Failed to send email!";
        }
    }


}
