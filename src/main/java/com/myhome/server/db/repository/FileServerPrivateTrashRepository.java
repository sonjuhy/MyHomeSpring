package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPrivateTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerPrivateTrashRepository extends JpaRepository<FileServerPrivateTrashEntity, Integer> {
    FileServerPrivateTrashEntity findByUuid(String uuid);
}
