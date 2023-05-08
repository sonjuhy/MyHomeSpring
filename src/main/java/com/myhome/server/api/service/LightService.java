package com.myhome.server.api.service;

import com.myhome.server.db.entity.LightEntity;

import java.util.List;

public interface LightService {
    LightEntity findByRoom(String room);
    List<LightEntity> findAll();
}
