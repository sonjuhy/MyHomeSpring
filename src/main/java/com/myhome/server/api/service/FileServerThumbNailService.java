package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerThumbNailEntity;

import java.io.File;
import java.util.List;

public interface FileServerThumbNailService {
    void deleteByUUID(String uuid);
    FileServerThumbNailEntity findByUUID(String uuid);
    void setThumbNail(List<File> files, String type);
    boolean makeThumbNail(File file, String uuid, String type);
    boolean checkThumbNailExist(String uuid);
}
