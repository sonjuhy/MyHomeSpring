package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPublicTrashRepository extends JpaRepository<FileServerPublicTrashEntity, String> {
    FileServerPublicTrashEntity findByUuid(String uuid);
    List<FileServerPublicTrashEntity> findAll();
    @Transactional
    int deleteByUuid(String uuid);
}
