package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerPublicRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

@Service
public class FileServerPublicServiceImpl implements FileServerPublicService {

    private final String diskPath;
    private final String trashPath;
    private final String thumbnailPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";
    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    KafkaProducer producer;

    LogComponent logComponent;

    @Autowired
    FileServerPublicRepository fileServerRepository;

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;

    @Autowired
    FileServerThumbNailService thumbNailService;

    @Autowired
    public FileServerPublicServiceImpl(FileDefaultPathRepository fileDefaultPathRepository, KafkaProducer kafkaProducer, LogComponent component){
        FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");
        FileDefaultPathEntity trashEntity = fileDefaultPathRepository.findByPathName("trash");
        FileDefaultPathEntity thumbnailEntity = fileDefaultPathRepository.findByPathName("thumbnail");
        diskPath = changeUnderBarToSeparator(storeEntity.getPublicDefaultPath());
        trashPath = changeUnderBarToSeparator(trashEntity.getPublicDefaultPath());
        thumbnailPath = changeUnderBarToSeparator(thumbnailEntity.getPublicDefaultPath());

        producer = kafkaProducer;
        logComponent = component;

        logComponent.sendLog("Cloud",
                "[FileServerPublicServiceImpl] diskPath : "+diskPath+", trashPath : "+trashPath+", thumbnailPath : " + thumbnailPath,
                true,
                TOPIC_CLOUD_LOG);
    }

    @Override
    public FileServerPublicEntity findByPath(String path) {
//        String originPath = changeUnderBarToSeparator(path);
        String originPath = path;
        if("default".equals(originPath)) originPath = diskPath;
        FileServerPublicEntity entity = fileServerRepository.findByPath(originPath);
        return entity;
    }

    @Override
    public FileServerPublicEntity findByUuidName(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        return entity;
    }

    @Override
    public List<FileServerPublicEntity> findByLocation(String location, int mode) {
//        String originLocation = changeUnderBarToSeparator(location);
//        System.out.println("location : " + originLocation);
        if("default".equals(location)) location = diskPath;
        List<FileServerPublicEntity> list = fileServerRepository.findByLocationAndDelete(location, mode);
        return list;
    }

    @Override
    public List<FileServerPublicEntity> findByLocationPage(String location, int mode, int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
//        String originLocation = changeUnderBarToSeparator(location);
        if("default".equals(location)) location = diskPath;
        List<FileServerPublicEntity> list = fileServerRepository.findByLocationAndDelete(location, mode, pageable);
        return list;
    }

    @Override
    public HttpHeaders getHttpHeader(Path path, String fileName) throws IOException {
        String contentType = Files.probeContentType(path); // content type setting

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition
                .builder("attachment") //builder type
//                .filename(entity.getOriginName(), StandardCharsets.UTF_8) // filename setting by utf-8
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        return httpHeaders;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String path, Model model) {

        String fileLocation = defaultUploadPath+File.separator+path+File.separator;
//        String originPath = changeUnderBarToSeparator(path);
        String originPath = path;
        if(originPath != null && originPath.isBlank() && !originPath.isEmpty()) {
            originPath += File.separator;
            //        String fileLocation = "C:\\Users\\SonJunHyeok\\Desktop\\test\\public"+File.separator+originPath;
            List<FileServerPublicEntity> list = new ArrayList<>();
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    try{
                        FileServerPublicDto dto = new FileServerPublicDto(
                                fileLocation+file.getOriginalFilename(), // file path (need to change)
                                file.getOriginalFilename(), // file name
                                UUID.randomUUID().toString(), // file name to change UUID
                                Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1), // file type (need to check ex: txt file -> text/plan)
                                (float)file.getSize(), // file size(KB)
                                fileLocation, // file folder path (need to change)
                                0,
                                0
                        );
                        System.out.println(file.getResource());
                        list.add(new FileServerPublicEntity(dto));
                        String saveName = fileLocation+dto.getName();
                        Path savePath = Paths.get(saveName);
                        file.transferTo(savePath);
                    } catch (IOException e) {
                        logComponent.sendErrorLog("Cloud", "[uploadFiles(public)] error : ", e, TOPIC_CLOUD_LOG);
                    }
                }
            }
            model.addAttribute("files", list);
            List<String> resultArr = new ArrayList<>();
            for (FileServerPublicEntity entity : list) {
                if (save(entity)) {
                    resultArr.add(entity.getName());
                }
            }
            logComponent.sendLog("Cloud", "[uploadFiles(public)] file size : "+resultArr.size(), true, TOPIC_CLOUD_LOG);
            return resultArr;
        }
        else{
            logComponent.sendLog("Cloud", "[uploadFiles(public)] uploadFile impl path is empty", false, TOPIC_CLOUD_LOG);
            return null;
        }
    }

    @Override
    public boolean mkdir(String path) {
        File file = new File(path);
        if(file.mkdir()){
            System.out.println("Public mkdir : " + path);

            String[] paths = path.split(File.separator);
            String name = paths[paths.length-1];
            StringBuilder location = new StringBuilder();
            for(int i=0;i<paths.length-1;i++){
                location.append(paths[i]).append(File.separator);
            }
            System.out.println("Public mkdir location : " + location.toString());

            FileServerPublicDto dto = new FileServerPublicDto(
                    path, // file path (need to change)
                    name, // file name
                    UUID.randomUUID().toString(), // file name to change UUID
                    "dir",
                    0, // file size(KB)
                    location.toString(), // file folder path (need to change)
                    0,
                    0
            );
            fileServerRepository.save(new FileServerPublicEntity(dto));
            logComponent.sendLog("Cloud", "[mkdir(public)] mkdir dto : "+dto, true, TOPIC_CLOUD_LOG);
            return true;
        }
        else{
            logComponent.sendLog("Cloud", "[mkdir(public)] failed to mkdir (path) : "+path, false, TOPIC_CLOUD_LOG);
            return false;
        }
    }

    @Override
    public boolean existsByPath(String path) {
//        String originPath = changeUnderBarToSeparator(path);
        boolean result = fileServerRepository.existsByPath(path);
        return result;
    }

    @Override
    public long deleteByPath(String path) {
//        String originPath = changeUnderBarToSeparator(path);
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[deleteByPath(public)] file entity is null (path) : "+path, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        // json type { file : origin file path, path : destination to move file }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("purpose", "delete");
        jsonObject.addProperty("action", "delete");
        jsonObject.addProperty("uuid", entity.getUuid());
        jsonObject.addProperty("file", trashPath+File.separator+entity.getName());
        jsonObject.addProperty("path", entity.getPath());
        String jsonResult = gson.toJson(jsonObject);
        System.out.println("deleteByPath : " + jsonResult);
        // kafka send
        producer.sendMessage(jsonResult);
        logComponent.sendLog("Cloud", "[deleteByPath(public)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
        return 0;
    }

    @Override
    public int moveFile(String path, String location) {
//        String originPath = changeUnderBarToSeparator(path);
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[moveFile(public)] file entity is null (path) : "+path+", location : " + location, false, TOPIC_CLOUD_LOG);
            return -1; // file info doesn't exist
        }
        // json type { file : origin file path, path : destination to move file }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("purpose", "move");
        jsonObject.addProperty("action", "move");
        jsonObject.addProperty("uuid", entity.getUuid());
        jsonObject.addProperty("file", path);
        jsonObject.addProperty("path", location);
        String jsonResult = gson.toJson(jsonObject);
        // kafka send
        producer.sendMessage(jsonResult);
        logComponent.sendLog("Cloud", "[moveFile(public)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
        return 0;
    }

    @Override
    public int moveTrash(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){
            // json type { file : origin file path, path : destination to move file }
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purpose", "move");
            jsonObject.addProperty("action", "delete");
            jsonObject.addProperty("uuid", entity.getUuid());
            jsonObject.addProperty("file", entity.getPath());
            jsonObject.addProperty("path", trashPath);
            String jsonResult = gson.toJson(jsonObject);
            // kafka send
            producer.sendMessage(jsonResult);
            logComponent.sendLog("Cloud", "[moveTrash(public)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        logComponent.sendLog("Cloud", "[moveTrash(public)] entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
        return -1;
    }

    @Override
    public int restore(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);

        if(entity != null){
            // json type { file : origin file path, path : destination to move file }
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purpose", "move");
            jsonObject.addProperty("action", "restore");
            jsonObject.addProperty("uuid", entity.getUuid());
            jsonObject.addProperty("file", trashPath+File.separator+entity.getName());
            jsonObject.addProperty("path", entity.getPath());
            String jsonResult = gson.toJson(jsonObject);
            // kafka send
            producer.sendMessage(jsonResult);
            logComponent.sendLog("Cloud", "[restore(public)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        logComponent.sendLog("Cloud", "[restore(public)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
        return -1;
    }

    @Override
    public int updateByFileServerPublicEntity(FileServerPublicEntity entity) {
        if(existsByPath(entity.getPath())){
            logComponent.sendLog("Cloud", "[updateByFileServerPublicEntity(public)] already exist file in location (path): "+entity.getPath(), false, TOPIC_CLOUD_LOG);
            return -1;
        }
        else{
            FileServerPublicEntity resultEntity = fileServerRepository.save(entity);
            if(ObjectUtils.isEmpty(resultEntity)){ // error during change info on DB
                logComponent.sendLog("Cloud", "[updateByFileServerPublicEntity(public)] Error during change info on DB (uuid): "+entity.getUuid(), false, TOPIC_CLOUD_LOG);
                return -2;
            }
            else{ // success update file info
                logComponent.sendLog("Cloud", "[updateByFileServerPublicEntity(public)] change info on DB (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_LOG);
                return 0;
            }
        }
    }

    @Override
    public boolean save(FileServerPublicEntity entity) {

        return !ObjectUtils.isEmpty(fileServerRepository.save(entity));
    }

    @Transactional
    @Override
    public void publicFileStateCheck() {
        fileServerRepository.updateAllStateToOne();
        traversalFolder(diskPath, true);
        traversalFolder(trashPath, false);
        deleteThumbNail();
        fileServerRepository.deleteByState(0);
    }
    private String changeUnderBarToSeparator(String path){
        return path.replaceAll("__", Matcher.quoteReplacement(File.separator));
    }
    private String changeSeparatorToUnderBar(String path){
        return path.replaceAll(Matcher.quoteReplacement(File.separator), "__");
    }

    private void traversalFolder(String path, boolean mode){
        System.out.println("This is Path : " + path);
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(files != null){
            ArrayList<String> dirList = new ArrayList<>();
            System.out.println("Files size : " + files.length);
            for(File file : files){
                try{
                    String type, extension;
                    if(file.isDirectory()) {
                        type = "dir : ";
                        extension = "dir";
                        dirList.add(file.getName());
                    }
                    else {
                        type = "file : ";
                        extension = file.getName().substring(file.getName().lastIndexOf(".") + 1); // file type (need to check ex: txt file -> text/plan)
                    }
//                    FileServerPublicEntity entity = fileServerRepository.findByPath(file.getPath());
                    int deleteStatus = 0;
                    String tmpPath = changeSeparatorToUnderBar(file.getPath()), tmpLocation = changeSeparatorToUnderBar(file.getPath().split(file.getName())[0]);
                    if(!mode) {
                        deleteStatus = 1;
                    }
//                    if(entity == null){
                    if(true){
                        String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();
                        FileServerPublicDto dto = new FileServerPublicDto(
                                tmpPath, // file path
                                file.getName(), // file name
                                uuid, // file name to change UUID
                                extension,
                                (float)(file.length()/1024), // file size(KB)
                                tmpLocation, // file folder path (location)
                                1,
                                deleteStatus
                        );
//                        fileServerRepository.save(new FileServerPublicEntity(dto));
                        if(Arrays.asList(videoExtensionList).contains(extension)){
                            thumbNailService.makeThumbNail(file, uuid, "public");
                        }
//                        logComponent.sendLog("Cloud-Check", "[traversalFolder(public)] file (dto) : "+dto+", no exist file", true, TOPIC_CLOUD_CHECK_LOG);
                    }
                    else{
//                        logComponent.sendLog("Cloud-Check", "[traversalFolder(public)] file (path) : "+file.getPath()+", (name) : "+type+file.getName()+", exist file", true, TOPIC_CLOUD_CHECK_LOG);
                    }
                }
                catch (Exception e){
//                    logComponent.sendErrorLog("Cloud-Check", "[traversalFolder(public)] file check error : ", e, TOPIC_CLOUD_CHECK_LOG);
                }
            }
            for(String folder : dirList){
                traversalFolder(path+File.separator+folder, mode);
            }
        }

    }

    @Transactional
    private void deleteThumbNail(){
        List<FileServerThumbNailEntity> thumbNailEntityList = thumbNailRepository.findAllNotInPublic();
        for(FileServerThumbNailEntity entity : thumbNailEntityList){
            String path = changeUnderBarToSeparator(entity.getPath());
            File thumbnailFile = new File(path);
            if(thumbnailFile.exists()){
                if(thumbnailFile.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(public)] files is deleted (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_CHECK_LOG);
                }
                else{
                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(public)] files is doesn't delete (uuid) : "+entity.getUuid(), false, TOPIC_CLOUD_CHECK_LOG);
                }
            }
        }
    }
}
