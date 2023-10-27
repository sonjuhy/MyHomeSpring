package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.db.entity.LightEntity;
import com.myhome.server.db.repository.LightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightServiceImpl implements LightService {

    @Autowired
    LightRepository repository;

    @Autowired
    KafkaProducer producer;


    @Override
    public LightEntity findByRoom(String room) {
        LightEntity entity = repository.findByRoom(room);
        return entity;
    }

    @Override
    public List<LightEntity> findAll() {
        List<LightEntity> list = repository.findAll();
        return list;
    }

    @Override
    public List<LightEntity> findByCategory(String category) {
        List<LightEntity> list = repository.findByCategory(category);
        return list;
    }

    @Override
    public void control(LightDto dto, String user) {
        String msg = dto.toString();
        System.out.println(msg);
        if(dto.getState().equals("On")) dto.setState("OFF");
        else if(dto.getState().equals("Off")) dto.setState("ON");
//        producer.sendMessage(msg);
        producer.sendIotMessage(dto, user);
    }
}
