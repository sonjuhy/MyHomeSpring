package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileServerPrivateService {
    FileServerPrivateEntity findByPath(String path);
    FileServerPrivateEntity findByUuid(String uuid);
    List<FileServerPrivateEntity> findByLocation(String location, int mode);
    List<FileServerPrivateEntity> findByOwner(String owner);
    HttpHeaders getHttpHeaders(Path path, String fileName) throws IOException;
    List<String> uploadFiles(MultipartFile[] files, String path, String token, Model model);
    boolean mkdir(String path, String token);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path, String accessToken);
    int moveFile(String path, String location, String accessToken);
    int moveTrash(String uuid, String accessToken);
    int restore(String uuid, String accessToken);
    int updateByFileServerPublicEntity(FileServerPrivateEntity entity);
    boolean save(FileServerPrivateEntity entity);
    void privateFileCheck();
}
