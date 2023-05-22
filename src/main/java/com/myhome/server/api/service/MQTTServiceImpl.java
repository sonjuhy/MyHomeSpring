package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.MQTTSendMessageDto;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQTTServiceImpl implements MQTTService{

//    private final String pubTopic = "MyHome/Light/Pub/Server";
    private final String pubTopic = "test";

    @Autowired
    private IMqttClient mqttClient;

    @Override
    public void publish(MQTTSendMessageDto dto) {
        Gson gson = new Gson();
        String dtoPayload = gson.toJson(dto);
        JsonObject object = new JsonObject();
        object.addProperty("Light", dtoPayload);
        String payload = gson.toJson(object).replace("\\","");

        int qos = 0;
        boolean retained = false;

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(payload.getBytes());
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(retained);

        try {
            mqttClient.publish(pubTopic, mqttMessage);
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean connected() {
        return mqttClient.isConnected();
    }

    @Override
    public void reconnect() {
        try {
            System.out.println("try to reconnect mqtt server");
            mqttClient.reconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
