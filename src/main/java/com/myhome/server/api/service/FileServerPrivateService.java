package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPrivateEntity;

import javax.transaction.Transactional;
import java.util.List;

public interface FileServerPrivateService {
    FileServerPrivateEntity findByPath(String path);
    List<FileServerPrivateEntity> findByLocation(String location);
    List<FileServerPrivateEntity> findByOwner(String owner);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path);
    int moveFile(String path, String location);
    int updateByFileServerPublicEntity(FileServerPrivateEntity entity);
    boolean save(FileServerPrivateEntity entity);
    void privateFileCheck();
}
