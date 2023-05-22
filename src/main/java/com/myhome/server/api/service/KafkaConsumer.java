package com.myhome.server.api.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "exam-topic", groupId = "spring-kafka")
    public void consume(String message) throws Exception {
        System.out.println("Consume message : " + message);
    }
}
