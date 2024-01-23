package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPublicEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileServerPublicService {
    FileServerPublicEntity findByPath(String path);
    FileServerPublicEntity findByUuidName(String uuid);
    List<FileServerPublicEntity> findByLocation(String location, int mode);
    List<FileServerPublicEntity> findByLocationPage(String location, int mode, int size, int page);
    HttpHeaders getHttpHeader(Path path, String fileName) throws IOException;
    HttpHeaders getHttpHeaderForVideo(Path path, String fileName, long fileSize) throws IOException;
    ResponseEntity<Resource> downloadFile(String uuid);
    ResponseEntity<Resource> downloadPublicMedia(String uuid);
    ResponseEntity<ResourceRegion> streamingPublicVideo(String uuid);
    List<String> uploadFiles(MultipartFile[] files, String path, Model model);
    boolean mkdir(String path);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path);
    int moveFile(String path, String location);
    int moveTrash(String uuid);
    int restore(String uuid);
    String encodingJSON(String purpose, String action, String uuid, String file, String path);
    int updateByFileServerPublicEntity(FileServerPublicEntity entity);
    boolean save(FileServerPublicEntity entity);
    void publicFileStateCheck();
    void filesWalk(String pathUrl);
    void filesWalkTrashPath(String pathUrl);
    void deleteThumbNail();
}
