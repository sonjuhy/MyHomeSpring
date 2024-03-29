package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.LightEntity;
import com.myhome.server.db.repository.LightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightServiceImpl implements LightService {

    private final String TOPIC_LIGHT_LOG = "iot-log-topic";

    @Autowired
    LogComponent logComponent;

    @Autowired
    LightRepository repository;

    @Autowired
    KafkaProducer producer;


    @Override
    public LightEntity findByRoom(String room) {
        LightEntity entity = repository.findByRoom(room);
        if(entity != null){
            logComponent.sendLog("Light", "[findByRoom] find light entity (entity) : "+entity+", room : " + room, true, TOPIC_LIGHT_LOG);
        }
        else{
            logComponent.sendLog("Light", "[findByRoom] entity is null (room) : " + room, false, TOPIC_LIGHT_LOG);
        }
        return entity;
    }

    @Override
    public List<LightEntity> findAll() {
        List<LightEntity> list = repository.findAll();
        if(list != null){
            logComponent.sendLog("Reserve", "[findAll] find all light list (list size) : "+list.size(), true, TOPIC_LIGHT_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[findAll] list is null", false, TOPIC_LIGHT_LOG);
        }
        return list;
    }

    @Override
    public List<LightEntity> findByCategory(String category) {
        List<LightEntity> list = repository.findByCategory(category);
        if(list != null){
            logComponent.sendLog("Reserve", "[findByCategory] find by category light list (list size) : "+list.size()+", category : "+category, true, TOPIC_LIGHT_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[findByCategory] list is null (category) : "+category, false, TOPIC_LIGHT_LOG);
        }
        return list;
    }

    @Override
    public void control(LightDto dto, String user) {
        String msg = dto.toString();
        System.out.println(msg);
        if(dto.getState().equals("On")) dto.setState("OFF");
        else if(dto.getState().equals("Off")) dto.setState("ON");
        producer.sendIotMessage(dto, user);
        logComponent.sendLog("Light", "[control] send control data (dto) : "+dto, true, TOPIC_LIGHT_LOG);
    }
}
