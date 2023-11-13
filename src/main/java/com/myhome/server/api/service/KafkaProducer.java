package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.LightDto;
import com.myhome.server.api.dto.LogReceiveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaProducer {
    private static final String TOPIC_TEST = "exam-topic";
    private static final String TOPIC_CLOUD = "cloud-topic";
    private static final String TOPIC_MQTT = "iot-topic";
    private static final String TOPIC_MQTT_RESERVE = "reserve-topic";

    @Autowired
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private final KafkaTemplate<String, LogReceiveDto> kafkaTemplateDto;

    public void sendMessage(String message){
        System.out.println("Produce message : "+message);
        this.kafkaTemplate.send(TOPIC_TEST, message);
    }

    public void sendIotMessage(LightDto dto, String user){
        Gson gson = new Gson();
        JsonObject object = new JsonObject();
        object.addProperty("sender", user);
        object.addProperty("message", dto.getState());
        object.addProperty("destination", dto.getRoom());
        object.addProperty("room", dto.getCategory());
        JsonObject packageObject = new JsonObject();
        packageObject.add("Light", object);

        String message = gson.toJson(packageObject);
        System.out.println(message);

        this.kafkaTemplate.send(TOPIC_MQTT, message);
    }

    public void sendLogDto(String service, String content, Boolean type, String topic){
        LogReceiveDto dto = new LogReceiveDto();
        dto.setId(0);
        dto.setSender("Spring");
        dto.setService(service);
        dto.setContent(content);
        dto.setType(type);
        this.kafkaTemplateDto.send(topic, dto);
    }

    public void sendReserveMessage(){
        this.kafkaTemplate.send(TOPIC_MQTT_RESERVE, "refresh");
    }
}
