package com.myhome.server.api.service;

import com.myhome.server.db.entity.LightRecordEntity;
import com.myhome.server.db.repository.LightRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightRecordServiceImpl implements LightRecordService{

    @Autowired
    LightRecordRepository repository;

    @Override
    public List<LightRecordEntity> findAll() {
        List<LightRecordEntity> list = repository.findAll();
        return list;
    }

    @Override
    public List<LightRecordEntity> findTop10ByRoomOrderByPkDesc(String room) {
        List<LightRecordEntity> list = repository.findTop10ByRoomOrderByPkDesc(room);
        return list;
    }
}
