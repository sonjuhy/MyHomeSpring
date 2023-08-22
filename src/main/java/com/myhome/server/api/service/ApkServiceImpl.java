package com.myhome.server.api.service;

import com.myhome.server.db.entity.ApkEntity;
import com.myhome.server.db.repository.ApkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ApkServiceImpl implements ApkService{

    @Autowired
    ApkRepository repository;

    @Override
    public HttpHeaders getHttpHeader(Path path) throws IOException {
        String apkName = "MyHome.apk";
        String contentType = Files.probeContentType(path); // content type setting

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition
                .builder("attachment") //builder type
//                .filename(entity.getOriginName(), StandardCharsets.UTF_8) // filename setting by utf-8
                .filename(apkName, StandardCharsets.UTF_8)
                .build());
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);

        return httpHeaders;
    }

    @Override
    public ApkEntity findByOrderByIdDesc() {
        ApkEntity entity = repository.findByOrderByIdDesc();
        return entity;
    }

    @Override
    public List<ApkEntity> findByAll() {
        List<ApkEntity> list = repository.findAll();
        return list;
    }

    @Override
    public double getLastVersion() {
        ApkEntity entity = findByOrderByIdDesc();
        return entity.getVersion();
    }
}
