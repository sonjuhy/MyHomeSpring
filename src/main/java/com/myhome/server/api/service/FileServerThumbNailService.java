package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerThumbNailEntity;

import java.io.File;

public interface FileServerThumbNailService {
    void deleteByUUID(String uuid);
    FileServerThumbNailEntity findByUUID(String uuid);
    void makeThumbNail(File file, String uuid, String type);
}
