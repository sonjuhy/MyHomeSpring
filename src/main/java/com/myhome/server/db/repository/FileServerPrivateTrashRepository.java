package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPrivateTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPrivateTrashRepository extends JpaRepository<FileServerPrivateTrashEntity, String> {
    FileServerPrivateTrashEntity findByUuid(String uuid);
    List<FileServerPrivateTrashEntity> findAll();
    @Transactional
    int deleteByUuid(String uuid);
}
