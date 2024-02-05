package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPrivateTrashEntity;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileServerPrivateService {
    FileServerPrivateEntity findByPath(String path);
    FileServerPrivateEntity findByUuid(String uuid);
    List<FileServerPrivateEntity> findByLocation(String location, int mode);
    List<FileServerPrivateEntity> findByLocationPage(String location, int mode, int size, int page);
    List<FileServerPrivateTrashEntity> findByLocationTrash(String location);
    List<FileServerPrivateTrashEntity> findByLocationPageTrash(String location, int size, int page);
    List<FileServerPrivateEntity> findByOwner(String owner);
    HttpHeaders getHttpHeaders(Path path, String fileName) throws IOException;
    ResponseEntity<Resource> downloadFile(String uuid);
    ResponseEntity<Resource> downloadPrivateMedia(String uuid);
    ResponseEntity<ResourceRegion> streamingPrivateVideo(HttpHeaders httpHeaders, String uuid);
    List<String> uploadFiles(MultipartFile[] files, String path, String token, Model model);
    boolean mkdir(String path, String token);
    boolean existsByPath(String path);
    @Transactional
    long deleteByPath(String path, String accessToken);
    int moveFile(String path, String location, String accessToken);
    int moveTrash(String uuid, String accessToken);
    int restore(String uuid, String accessToken);
    String encodingJSON(String purpose, String action, String uuid, String file, String path);
    int updateByFileServerPrivateEntity(FileServerPrivateEntity entity);
    boolean save(FileServerPrivateEntity entity);
    List<File> filesWalkWithReturnMediaFileList(String pathUrl, String owner);
    void privateFileCheck();
    void privateFileTrashCheck();
    void filesWalk(String pathUrl, String owner);
    void filesWalkTrash(String pathUrl, String owner);
    void deleteThumbNail();
}
