package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPrivateTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FileServerPrivateTrashRepository extends JpaRepository<FileServerPrivateTrashEntity, Integer> {
    FileServerPrivateTrashEntity findByUuid(String uuid);
    List<FileServerPrivateTrashEntity> findByLocation(String location);
    List<FileServerPrivateTrashEntity> findByLocation(String location, Pageable pageable);
}
