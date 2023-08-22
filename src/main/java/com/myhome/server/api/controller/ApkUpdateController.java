package com.myhome.server.api.controller;

import com.myhome.server.api.service.ApkService;
import com.myhome.server.api.service.ApkServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController()
@RequestMapping("/apkUpdate")
public class ApkUpdateController {

    @Autowired
    private ApkService service = new ApkServiceImpl();

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadApkFile(){
        String filePath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\MyHome.apk";
        Path path = Paths.get(filePath);
        try{
            HttpHeaders httpHeaders = service.getHttpHeader(path);
            Resource resource = new InputStreamResource(Files.newInputStream(path));
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(null, null, HttpStatus.BAD_GATEWAY);
    }

    @GetMapping("/versionCheck")
    public ResponseEntity<Double> versionCheck(){
        double version = service.getLastVersion();
        return new ResponseEntity<>(version, HttpStatus.OK);
    }
}
