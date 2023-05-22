package com.myhome.server.db.repository;

import com.myhome.server.db.entity.ComputerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComputerRepository extends JpaRepository<ComputerEntity, Integer> {
    ComputerEntity findByName(String name);
    List<ComputerEntity> findAll();
}
