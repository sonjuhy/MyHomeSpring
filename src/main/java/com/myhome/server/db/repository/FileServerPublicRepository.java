package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPublicRepository extends JpaRepository<FileServerPublicEntity, String> {
//    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    FileServerPublicEntity findByPath(String path);
    List<FileServerPublicEntity> findByLocation(String location);
//    boolean existsByFileServerPublicEntity(FileServerPublicEntity entity);
    @Transactional
    long deleteByPath(String path);
//    int updateByFileServerPublicEntity(FileServerPublicEntity entity);
}
