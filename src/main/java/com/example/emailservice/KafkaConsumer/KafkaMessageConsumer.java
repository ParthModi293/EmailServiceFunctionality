package com.example.emailservice.KafkaConsumer;

import com.example.emailservice.dto.EmailDto;
import com.example.emailservice.service.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageConsumer {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "email-topic",groupId = "email-group",containerFactory = "kafkaListenerContainerFactory")
    public void consumeEmail(EmailDto emailDto) throws MessagingException {


        emailService.sendEmail(emailDto);


    }


}
