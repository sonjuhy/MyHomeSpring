package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileServerPrivateService {
    FileServerPrivateEntity findByPath(String path);
    List<FileServerPrivateEntity> findByLocation(String location);
    List<FileServerPrivateEntity> findByOwner(String owner);
    HttpHeaders getHttpHeaders(Path path, String fileName) throws IOException;
    List<String> uploadFiles(MultipartFile[] files, String path, String token, Model model);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path);
    int moveFile(String path, String location);
    int updateByFileServerPublicEntity(FileServerPrivateEntity entity);
    boolean save(FileServerPrivateEntity entity);
    void privateFileCheck();
}
