package com.myhome.server.api.service;

import com.myhome.server.db.entity.ApkEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ApkService {
    HttpHeaders getHttpHeader(Path path) throws IOException;
    ApkEntity findByOrderByIdDesc();
    List<ApkEntity> findByAll();
    double getLastVersion();
}
