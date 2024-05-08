package com.myhome.server.api.service;

import com.myhome.server.db.entity.LightRecordEntity;

import java.util.List;

public interface LightRecordService {
    List<LightRecordEntity> findAll();
    List<LightRecordEntity> findTop10ByRoomOrderByPkDesc(String room);
}
