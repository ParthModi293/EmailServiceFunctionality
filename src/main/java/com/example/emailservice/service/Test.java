package com.example.emailservice.service;

import com.example.emailservice.bean.EmailBean;
import jakarta.persistence.Column;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class Test implements CommandLineRunner {
    private final EmailService emailService;

    public Test(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Loop start time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        for (int i = 0; i < 5; i++) {
            EmailBean emailBean = new EmailBean();
            emailBean.setDbName("test");
            emailBean.setTemplateName("sale");
            emailBean.setTo(Arrays.asList("zaboghara@gmail.com"));
            emailBean.setBcc(Collections.emptyList());
            emailBean.setCc(Collections.emptyList());

            Map<String, Object> subjectPlaceholders = new HashMap<>();
            subjectPlaceholders.put("name", "Zeel");
            subjectPlaceholders.put("Company", "Zeel");
            emailBean.setSubjectPlaceHolder(subjectPlaceholders);

            Map<String, Object> bodyPlaceholders = new HashMap<>();
            bodyPlaceholders.put("name", "Zeel");
            bodyPlaceholders.put("Company", "Zeel");
            emailBean.setBodyPlaceHolder(bodyPlaceholders);

            emailBean.setFile(Collections.emptyList());
            emailService.mailSender(emailBean);
        }
        System.out.println("All Mail sent");
        System.out.println("Loop end time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

    }
}
