package com.myhome.server.api.service;

import com.myhome.server.db.entity.LightEntity;
import com.myhome.server.db.repository.LightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightServiceImpl implements LightService {

    @Autowired
    LightRepository repository;

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
}
