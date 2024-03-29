package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.db.entity.LightEntity;

import java.util.List;

public interface LightService {
    LightEntity findByRoom(String room);
    List<LightEntity> findAll();
    List<LightEntity> findByCategory(String category);
    void control(LightDto dto, String user);
}
