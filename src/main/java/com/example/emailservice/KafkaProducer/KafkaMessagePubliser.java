package com.example.emailservice.KafkaProducer;

import com.example.emailservice.dto.EmailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessagePubliser {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(EmailDto emailDto,int partition) {
        kafkaTemplate.send(ProducerConfiguration.TOPIC_NAME,partition,null ,emailDto);
    }

}
