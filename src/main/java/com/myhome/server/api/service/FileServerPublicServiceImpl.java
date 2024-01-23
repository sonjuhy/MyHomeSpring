package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServerPublicServiceImpl implements FileServerPublicService {

    private final String diskPath;
    private final String trashPath;
    private final String thumbnailPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";
    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    KafkaProducer producer;

    LogComponent logComponent;

    @Autowired
    FileServerPublicRepository fileServerRepository;
    @Autowired
    FileServerPublicTrashRepository fileServerPublicTrashRepository;
    @Autowired
    FileServerCustomRepository fileServerCustomRepository;

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;
    @Autowired
    FileServerThumbNailService thumbNailService;
    @Autowired
    FileServerCommonService commonService;
    @Autowired
    public FileServerPublicServiceImpl(FileDefaultPathRepository fileDefaultPathRepository, FileServerCommonService commonService, KafkaProducer kafkaProducer, LogComponent component){
        FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");
        FileDefaultPathEntity trashEntity = fileDefaultPathRepository.findByPathName("trash");
        FileDefaultPathEntity thumbnailEntity = fileDefaultPathRepository.findByPathName("thumbnail");
        diskPath = commonService.changeUnderBarToSeparator(storeEntity.getPublicDefaultPath());
        trashPath = commonService.changeUnderBarToSeparator(trashEntity.getPublicDefaultPath());
        thumbnailPath = commonService.changeUnderBarToSeparator(thumbnailEntity.getPublicDefaultPath());

        producer = kafkaProducer;
        logComponent = component;

//        logComponent.sendLog("Cloud",
//                "[FileServerPublicServiceImpl] diskPath : "+diskPath+", trashPath : "+trashPath+", thumbnailPath : " + thumbnailPath,
//                true,
//                TOPIC_CLOUD_LOG);
    }

    @Override
    public FileServerPublicEntity findByPath(String path) {
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
        if("default".equals(location)) location = diskPath;
        List<FileServerPublicEntity> list = fileServerRepository.findByLocationAndDelete(location, mode);
        return list;
    }

    @Override
    public List<FileServerPublicEntity> findByLocationPage(String location, int mode, int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
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
                .filename(fileName)
                .build()
        );
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        return httpHeaders;
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){
            String pathStr = commonService.changeUnderBarToSeparator(entity.getPath());
            Path path = Paths.get(pathStr);
            try{
                HttpHeaders httpHeaders = getHttpHeader(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPublicFile error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        logComponent.sendLog("Cloud","downloadPublicFile error : file doesn't exist", false, TOPIC_CLOUD_LOG);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> downloadPublicMedia(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){
            String pathStr = commonService.changeUnderBarToSeparator(entity.getPath());
            Path path = Paths.get(pathStr);
            try{
                HttpHeaders httpHeaders = getHttpHeader(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path)); // save file resource
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPublicMedia error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        logComponent.sendLog("Cloud","downloadPublicMedia error : file doesn't exist", false, TOPIC_CLOUD_LOG);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResourceRegion> streamingPublicVideo(HttpHeaders httpHeaders, String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){
            String pathStr = commonService.changeUnderBarToSeparator(entity.getPath());
            Path path = Paths.get(pathStr);
            try{
                Resource resource = new FileSystemResource(path);
                long chunkSize = 1024*1024;
                long contentLength = resource.contentLength();
                ResourceRegion resourceRegion;
                try{
                    HttpRange httpRange;
                    if(httpHeaders.getRange().stream().findFirst().isPresent()){
                        httpRange = httpHeaders.getRange().stream().findFirst().get();
                    }
                    else{
                        httpRange = httpHeaders.getRange().get(0);
                    }
                    for(HttpRange range : httpHeaders.getRange()){
                        System.out.println(range.toString());
                    }

                    long start = httpRange.getRangeStart(contentLength);
                    long end = httpRange.getRangeEnd(contentLength);
                    long rangeLength = Long.min(chunkSize, end-start+1);
                    System.out.println("contentLength : "+contentLength+", start : "+start+", end : "+end+", rangeLength : "+rangeLength);

                    resourceRegion = new ResourceRegion(resource, start, rangeLength);
                    System.out.println("resourceRegion size : " + resourceRegion.getResource().contentLength());
                }
                catch(Exception e){
                    System.out.println("StreamPublicVideo Exception e : "+e.getMessage());
                    e.printStackTrace();
                    long rangeLength = Long.min(chunkSize, resource.contentLength());
                    resourceRegion = new ResourceRegion(resource, 0, rangeLength);
                }
               return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                       .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES)) // 10분
                       .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                       .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                       .body(resourceRegion);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","streamingPublicVideo error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        logComponent.sendLog("Cloud","streamingPublicVideo error : file doesn't exist", false, TOPIC_CLOUD_LOG);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String path, Model model) {
        if(path != null && path.isBlank() && !path.isEmpty()) {
            path += File.separator;
            String originPath = commonService.changeSeparatorToUnderBar(path);
            List<FileServerPublicEntity> list = new ArrayList<>();
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    try{
                        String tmpPath = commonService.changeSeparatorToUnderBar(originPath+file.getOriginalFilename());
                        String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();

                        FileServerPublicDto dto = new FileServerPublicDto(
                                tmpPath,
                                file.getOriginalFilename(), // file name
                                uuid, // file name to change UUID
                                Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1), // file type (need to check ex: txt file -> text/plan)
                                (float)file.getSize(), // file size(KB)
                                originPath, // file folder path (need to change)
                                0,
                                0
                        );
                        list.add(new FileServerPublicEntity(dto));
                        String saveName = path+dto.getName();
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
        String originPath = commonService.changeUnderBarToSeparator(path);
        File file = new File(originPath);
        if(file.mkdir()){
            String underBar = "__";
            String[] paths = path.split(underBar);
            String name = paths[paths.length-1];
            StringBuilder location = new StringBuilder();
            for(int i=0;i<paths.length-1;i++){
                location.append(paths[i]).append(underBar);
            }
            String uuid = UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)).toString();
            FileServerPublicDto dto = new FileServerPublicDto(
                    path, // file path (need to change)
                    name, // file name
                    uuid, // file name to change UUID
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
        String jsonResult = encodingJSON("delete", "delete", entity.getUuid(), entity.getPath(), entity.getLocation());
        System.out.println("deleteByPath : " + jsonResult);
        // kafka send
        producer.sendCloudMessage(jsonResult);
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
        String movePath = location + entity.getPath();
        // json type { file : origin file path, path : destination to move file }
        String jsonResult = encodingJSON("move", "move", entity.getUuid(), path, movePath);
        // kafka send
        producer.sendCloudMessage(jsonResult);
        logComponent.sendLog("Cloud", "[moveFile(public)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
        return 0;
    }

    @Override
    public int moveTrash(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){
            String underBar = "__";
            String tmpTrashPath = commonService.changeSeparatorToUnderBar(trashPath)+underBar;
            // json type { file : origin file path, path : destination to move file }
            String jsonResult = encodingJSON("move", "delete", entity.getUuid(), entity.getPath(), tmpTrashPath);
            // kafka send
            producer.sendCloudMessage(jsonResult);
            logComponent.sendLog("Cloud", "[moveTrash(public)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        logComponent.sendLog("Cloud", "[moveTrash(public)] entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
        return -1;
    }

    @Override
    public int restore(String uuid) {
        FileServerPublicTrashEntity trashEntity = fileServerPublicTrashRepository.findByUuid(uuid);
        if(trashEntity != null){
            // json type { file : origin file path, path : destination to move file }
            String jsonResult = encodingJSON("move", "restore", trashEntity.getUuid(), trashEntity.getPath(), trashEntity.getOriginPath());
            // kafka send
            producer.sendCloudMessage(jsonResult);
            logComponent.sendLog("Cloud", "[restore(public)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        logComponent.sendLog("Cloud", "[restore(public)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
        return -1;
    }

    @Override
    public String encodingJSON(String purpose, String action, String uuid, String file, String path) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("purpose", purpose);
        jsonObject.addProperty("action", action);
        jsonObject.addProperty("uuid", uuid);
        jsonObject.addProperty("file", file);
        jsonObject.addProperty("path", path);
        return gson.toJson(jsonObject);
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

    @Override
    public void publicFileStateCheck() {
        filesWalk(diskPath);
        filesWalkTrashPath(trashPath);
        deleteThumbNail();
    }
    @Override
    public void filesWalk(String pathUrl){
        Path originPath = Paths.get(pathUrl);
        List<Path> pathList;
        try{
            Stream<Path> pathStream = Files.walk(originPath);
            pathList = pathStream.collect(Collectors.toList());
            List<FileServerPublicDto> fileList = new ArrayList<>();
            List<File> mediaFileList = new ArrayList<>();
            for(Path path : pathList){
                File file = new File(path.toString());
                String extension = "dir";
                if(!file.isDirectory()) {
                    extension = file.getName().substring(file.getName().lastIndexOf(".") + 1); // file type (need to check ex: txt file -> text/plan)
                }
                try {
                    String tmpPath = commonService.changeSeparatorToUnderBar(file.getPath()), tmpLocation = commonService.changeSeparatorToUnderBar(file.getPath().split(file.getName())[0]);

                    String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();
                    fileList.add(new FileServerPublicDto(
                            tmpPath,
                            file.getName(),
                            uuid,
                            extension,
                            (float) (file.length() / 1024),
                            tmpLocation,
                            1,
                            0
                    ));
                    if (Arrays.asList(videoExtensionList).contains(extension) && !thumbNailRepository.existsByUuid(uuid)) {
                        mediaFileList.add(file);
                    }
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
            fileServerCustomRepository.saveBatchPublic(fileList);
            for(File file : mediaFileList){
                thumbNailService.makeThumbNail(file,
                        UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString(),
                        "public"
                );
            }
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[filesWalk(public)] file check error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
    }
    @Transactional
    @Override
    public void filesWalkTrashPath(String pathUrl){
        /*
        * 1. load all data from db
        * 2. compare file list
        * 3. if file is existed, data is not existed - new data, origin data  = default path
        * 4. if file is not existed, data is existed - delete data
        * */
        List<FileServerPublicTrashEntity> list = fileServerPublicTrashRepository.findAll();
        Path originPath = Paths.get(pathUrl);
        List<Path> pathList;
        try{
            Stream<Path> pathStream = Files.walk(originPath);
            pathList = pathStream.collect(Collectors.toList());
            List<FileServerPublicTrashEntity> existFileList = new ArrayList<>();
            List<FileServerPublicTrashEntity> newFileList = new ArrayList<>();
            for(Path path : pathList){
                File file = path.toFile();
                try{
                    String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
                    List<FileServerPublicTrashEntity> filterList = list.stream()
                            .filter(entity -> uuid.equals(entity.getUuid()))
                            .toList();
                    if(filterList.isEmpty()){
                        String tmpPath = commonService.changeSeparatorToUnderBar(file.getPath()), tmpLocation = commonService.changeSeparatorToUnderBar(file.getPath().split(file.getName())[0]);
                        String extension = "dir";
                        if(!file.isDirectory()) {
                            extension = file.getName().substring(file.getName().lastIndexOf(".") + 1); // file type (need to check ex: txt file -> text/plan)
                        }
                        newFileList.add(new FileServerPublicTrashEntity(
                                0,
                                uuid,
                                tmpPath,
                                commonService.changeSeparatorToUnderBar(diskPath),
                                file.getName(),
                                extension,
                                (float) (file.length() / 1024),
                                tmpLocation,
                                0
                        ));
                    }
                    else{
                        existFileList.addAll(filterList); // db data, file both exist
                    }
                }
                catch(Exception e){
                    System.out.println("fileswalkTrash public error : "+e.getMessage());
                }

            }
            for(FileServerPublicTrashEntity entity : existFileList){
                list.remove(entity);
            }
            fileServerPublicTrashRepository.deleteAll(list);
            fileServerPublicTrashRepository.saveAll(newFileList);
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[filesWalkTrash(public)] file check error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
    }
    @Transactional
    @Override
    public void deleteThumbNail(){
        List<FileServerThumbNailEntity> thumbNailEntityList = thumbNailRepository.findAllNotInPublic();
        for(FileServerThumbNailEntity entity : thumbNailEntityList){
            String path = commonService.changeUnderBarToSeparator(entity.getPath());
            File thumbnailFile = new File(path);
            if(thumbnailFile.exists()){
                if(thumbnailFile.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
//                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(public)] files is deleted (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_CHECK_LOG);
                }
                else{
                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(public)] files is doesn't delete (uuid) : "+entity.getUuid(), false, TOPIC_CLOUD_CHECK_LOG);
                }
            }
        }
    }
}
