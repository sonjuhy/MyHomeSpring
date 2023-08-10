package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightReserveDto;
import com.myhome.server.db.entity.LightReserveEntity;

import java.util.List;

public interface LightReserveService {
    LightReserveEntity findByPk(int pk);
    List<LightReserveEntity> findByRoom(String room);
    List<LightReserveEntity> findAll();
    void save(LightReserveDto dto);
    void updateReserve(LightReserveDto dto);
    void deleteReserve(int pk);
}
