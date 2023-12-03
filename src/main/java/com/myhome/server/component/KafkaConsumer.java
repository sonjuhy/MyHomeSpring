package com.myhome.server.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.FileServerPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @Autowired
    FileServerPublicService publicService;
    @Autowired
    FileServerPrivateService privateService;

    @KafkaListener(topics = "exam-topic", groupId = "spring-group")
    public void consume(String message) throws Exception {
        System.out.println("Consume message : " + message);
    }

    @KafkaListener(topics = "cloud-check", groupId = "spring-group")
    public void cloudConsume(String message) throws Exception{
        System.out.println("Cloud Consume meesage : " + message);
        JsonObject object = (JsonObject) JsonParser.parseString(message);
        String value = object.get("messages").getAsString();
        if("check".equals(value)){
            //run file check
            System.out.println("Running file check...");
        }
    }
}