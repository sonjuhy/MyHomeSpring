package com.myhome.server.db.repository;

import com.myhome.server.db.entity.LightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LightRepository extends JpaRepository<LightEntity, String> {
    LightEntity findByRoom(String room);
    List<LightEntity> findAll();
}
