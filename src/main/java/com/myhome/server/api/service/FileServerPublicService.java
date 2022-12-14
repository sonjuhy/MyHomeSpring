package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPublicEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface FileServerPublicService {
    FileServerPublicEntity findByPath(String path);
    List<FileServerPublicEntity> findByLocation(String location);
    boolean existsByFileServerPublicEntity(FileServerPublicEntity entity);
    @Transactional
    long deleteByPath(String path);
    int updateByFileServerPublicEntity(FileServerPublicEntity entity);
    boolean save(FileServerPublicEntity entity);
}
