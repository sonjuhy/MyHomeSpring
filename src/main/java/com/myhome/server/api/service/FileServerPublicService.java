package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPublicEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface FileServerPublicService {
    FileServerPublicEntity findByPath(String path);
    FileServerPublicEntity findByUuidName(String uuid);
    List<FileServerPublicEntity> findByLocation(String location);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path);
    int moveFile(String path, String location);
    int updateByFileServerPublicEntity(FileServerPublicEntity entity);
    boolean save(FileServerPublicEntity entity);
    void publicFileStateCheck();
}
