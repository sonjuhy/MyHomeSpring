package com.myhome.server.db.repository;

import com.myhome.server.db.entity.LightReserveEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LightReserveRepository extends JpaRepository<LightReserveEntity, Integer> {
    List<LightReserveEntity> findByRoom(String room);
    LightReserveEntity findByPk(int pk);
}
