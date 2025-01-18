package com.example.emailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EmailServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }


//    For Kafka ------------------>>>>>>>>>>>>>.
//    https://github.com/givanthak/webtrekk-email-service/blob/master/src/main/java/com/webtrekk/email/service/api/EmailController.java

}
