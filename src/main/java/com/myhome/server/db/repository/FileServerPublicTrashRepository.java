package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerPublicTrashRepository extends JpaRepository<FileServerPublicTrashEntity, Integer> {
    FileServerPublicTrashEntity findByUuid(String uuid);
}
