package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicTrashEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FileServerPublicTrashRepository extends JpaRepository<FileServerPublicTrashEntity, Integer> {
    FileServerPublicTrashEntity findByUuid(String uuid);
    List<FileServerPublicTrashEntity> findByLocation(String location);
    List<FileServerPublicTrashEntity> findByLocation(String location, Pageable pageable);
}
