package com.myhome.server.db.repository;

import com.myhome.server.db.entity.ApkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApkRepository extends JpaRepository<ApkEntity, Integer> {
    ApkEntity findByOrderByIdDesc();
    List<ApkEntity> findAll();
}
