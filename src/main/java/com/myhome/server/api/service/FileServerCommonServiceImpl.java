package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;

@Service
public class FileServerCommonServiceImpl implements FileServerCommonService{

    private final static String defaultVideoIconPath = "__home__disk1__home__setting__video.png";
    private final static String defaultImageIconPath = "__home__disk1__home__setting__image.png";

    @Autowired
    private FileDefaultPathRepository defaultPathRepository;
    @Autowired
    private FileServerThumbNailService thumbNailService;

    @Override
    public int[] getStorageUsage(String mode) {
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("top");
        String topPath = entity.getPublicDefaultPath();
        File file = new File(changeUnderBarToSeparator(topPath));
        int[] usage = new int[2];
        switch(mode){
            case "MB":
                usage[0] = toMB(file.getTotalSpace());
                usage[1] = toMB(file.getFreeSpace());
                break;
            case "GB":
                usage[0] = toGB(file.getTotalSpace());
                usage[1] = toGB(file.getFreeSpace());
                break;
            case "percent":
                int total = toGB(file.getTotalSpace());
                int free = toGB(file.getFreeSpace());
                usage[0] = 100 - (int) ((free*1.0)/total*100);
                usage[1] = (int) ((free*1.0)/total*100);
                break;
        }
        return usage;
    }

    @Override
    public Resource getDefaultVideoIconFile() {
        try {
            Path path = Paths.get(changeUnderBarToSeparator(defaultVideoIconPath));
            String fileName = "defaultVideoIcon";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition
                    .builder("attachment") //builder type
                    .filename(fileName)
                    .build()
            );
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path));
            return new InputStreamResource(Files.newInputStream(path));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Resource getDefaultImageIconFile() {
        try {
            Path path = Paths.get(changeUnderBarToSeparator(defaultImageIconPath));
            String fileName = "defaultImageIcon";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition
                    .builder("attachment") //builder type
                    .filename(fileName)
                    .build()
            );
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path));
            return new InputStreamResource(Files.newInputStream(path));
        }
        catch (Exception e){
            return null;
        }
    }

    public int toMB(long size){
        return (int)(size/(1024*1024));
    }
    public int toGB(long size){
        return (int)(size/(1024*1024*1024));
    }
    @Override
    public String changeUnderBarToSeparator(String path){
        return path.replaceAll("__", Matcher.quoteReplacement(File.separator));
    }
    @Override
    public String changeSeparatorToUnderBar(String path){
        return path.replaceAll(Matcher.quoteReplacement(File.separator), "__");
    }

    @Override
    public ResponseEntity<Resource> downloadThumbNail(String uuid) {
        FileDefaultPathEntity defaultPathEntity = defaultPathRepository.findByPathName("thumbnail");
        String thumbNailPath = defaultPathEntity.getPublicDefaultPath();
        FileServerThumbNailEntity thumbNailEntity = thumbNailService.findByUUID(uuid);
        Path path;
        String fileName;
        if(thumbNailEntity != null){
            path = Paths.get(changeUnderBarToSeparator(thumbNailEntity.getPath()));
            fileName = thumbNailEntity.getPath().replace(thumbNailPath, "");
        }
        else{
            path = Paths.get(changeUnderBarToSeparator(defaultVideoIconPath));
            fileName = "defaultVideoIcon";
        }

        try{
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(ContentDisposition
                    .builder("attachment") //builder type
                    .filename(fileName)
                    .build()
            );
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path));
            Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
            return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
        }
        catch(IOException e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
