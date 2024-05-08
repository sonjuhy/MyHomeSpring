package com.myhome.server.db.repository;

import com.myhome.server.db.entity.LightRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LightRecordRepository extends JpaRepository<LightRecordEntity, Integer> {
    List<LightRecordEntity> findAll();
    List<LightRecordEntity> findTop10ByRoomOrderByPkDesc(String room);
}
