package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileServerPrivateServiceImpl implements FileServerPrivateService {

    private final String diskPath;
    private final String trashPath;
    private final String thumbnailPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";
    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    KafkaProducer producer;
    LogComponent logComponent;

    @Autowired
    FileServerPrivateRepository repository;
    @Autowired
    FileServerPrivateTrashRepository trashRepository;

    @Autowired
    UserService service;

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;
    @Autowired
    FileServerCustomRepository fileServerCustomRepository;
    @Autowired
    FileServerThumbNailService thumbNailService;
    @Autowired
    FileServerCommonService commonService;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    public FileServerPrivateServiceImpl(FileDefaultPathRepository fileDefaultPathRepository, FileServerCommonService commonService, KafkaProducer kafkaProducer, LogComponent component){
        FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");
        FileDefaultPathEntity trashEntity = fileDefaultPathRepository.findByPathName("trash");
        FileDefaultPathEntity thumbnailEntity = fileDefaultPathRepository.findByPathName("thumbnail");
        diskPath = commonService.changeUnderBarToSeparator(storeEntity.getPrivateDefaultPath());
        trashPath = commonService.changeUnderBarToSeparator(trashEntity.getPrivateDefaultPath());
        thumbnailPath = commonService.changeUnderBarToSeparator(thumbnailEntity.getPrivateDefaultPath());

        producer = kafkaProducer;
        logComponent = component;
        logComponent.sendLog("Cloud",
                "[FileServerPrivateServiceImpl] diskPath : "+diskPath+", trashPath : "+trashPath+", thumbnailPath : " + thumbnailPath,
                true,
                TOPIC_CLOUD_LOG);

    }

    @Override
    public FileServerPrivateEntity findByPath(String path) {
//        String originPath = changeUnderBarToSeparator(path);
        FileServerPrivateEntity entity = repository.findByPath(path);
        return entity;
    }

    @Override
    public FileServerPrivateEntity findByUuid(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        return entity;
    }

    @Override
    public List<FileServerPrivateEntity> findByLocation(String location, int mode) {
//        String originLocation = changeUnderBarToSeparator(location);
        String originLocation = location;
        if("default".equals(originLocation)) originLocation = diskPath;
        List<FileServerPrivateEntity> list = repository.findByLocationAndDelete(originLocation, mode);
        return list;
    }

    @Override
    public List<FileServerPrivateEntity> findByLocationPage(String location, int mode, int size, int page) {
        Pageable pageable = PageRequest.of(page, size);
//        String originLocation = changeUnderBarToSeparator(location);
        String originLocation = location;
        if("default".equals(originLocation)) originLocation = diskPath;
        List<FileServerPrivateEntity> list = repository.findByLocationAndDelete(originLocation, mode, pageable);
        return list;
    }

    @Override
    public List<FileServerPrivateEntity> findByOwner(String owner) {
        List<FileServerPrivateEntity> list = repository.findByOwner(owner);
        return list;
    }

    @Override
    public HttpHeaders getHttpHeaders(Path path, String fileName) throws IOException {
        String contentType = Files.probeContentType(path); // content type setting
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentDisposition(ContentDisposition
                .builder("attachment") //builder type
                .filename(fileName)
                .build());
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        return httpHeaders;
    }

    @Override
    public ResponseEntity<Resource> downloadFile(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(entity!= null){
            String pathStr = commonService.changeUnderBarToSeparator(entity.getPath());
            Path path = Paths.get(pathStr);
            System.out.println("path : "+path.toFile().getPath());
            try {
                HttpHeaders httpHeaders = getHttpHeaders(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path));
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPrivateFile error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        logComponent.sendLog("Cloud","downloadPrivateFile error : file doesn't exist", false, TOPIC_CLOUD_LOG);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> downloadPrivateMedia(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(entity!= null){
            String pathStr = commonService.changeUnderBarToSeparator(entity.getPath());
            Path path = Paths.get(pathStr);
            try {
                HttpHeaders httpHeaders = getHttpHeaders(path, entity.getName());
                Resource resource = new InputStreamResource(Files.newInputStream(path));
                return new ResponseEntity<>(resource, httpHeaders, HttpStatus.OK);
            } catch (IOException e) {
                logComponent.sendErrorLog("Cloud","downloadPrivateMedia error : ", e, TOPIC_CLOUD_LOG);
                return new ResponseEntity<>(null, HttpStatus.OK);
            }
        }
        logComponent.sendLog("Cloud","downloadPrivateMedia error : file doesn't exist", false, TOPIC_CLOUD_LOG);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String path, String token, Model model) {
        if(path != null && path.isBlank() && !path.isEmpty()) {
            boolean result = jwtTokenProvider.validateToken(token);
            if(result){
                String id = jwtTokenProvider.getUserPk(token);
                Optional<UserEntity> userEntity = service.findById(id);
                List<FileServerPrivateEntity> list = new ArrayList<>();
                path += File.separator;
                String originPath = commonService.changeSeparatorToUnderBar(path);
                for(MultipartFile file : files){
                    if(!file.isEmpty()){
                        try{
                            String tmpPath = commonService.changeSeparatorToUnderBar(originPath+file.getOriginalFilename());
                            String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();

                            FileServerPrivateDto dto = new FileServerPrivateDto(
                                    tmpPath,
                                    file.getOriginalFilename(), // file name
                                    uuid,
                                    Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1), // file type (need to check ex: txt file -> text/plan)
                                    (float)file.getSize(), // file size(KB)
                                    userEntity.get().getName(),
                                    originPath, // file folder path (need to change)
                                    0,
                                    0
                            );
                            list.add(new FileServerPrivateEntity(dto));
                            String saveName = path +dto.getName();
                            Path savePath = Paths.get(saveName);
                            file.transferTo(savePath);
                        } catch (IOException e) {
                            logComponent.sendErrorLog("Cloud", "[uploadFiles(private)] error : ", e, TOPIC_CLOUD_CHECK_LOG);
                        }
                    }
                }
                model.addAttribute("files", list);
                List<String> resultArr = new ArrayList<>();
                for (FileServerPrivateEntity entity : list) {
                    if (save(entity)) {
                        resultArr.add(entity.getName());
                    }
                }
                logComponent.sendLog("Cloud", "[uploadFiles(private)] file size : "+resultArr.size(), true, TOPIC_CLOUD_LOG);
                return resultArr;
            }
            else{
                logComponent.sendLog("Cloud", "[uploadFiles(private)] jwtToken validate failed", false, TOPIC_CLOUD_LOG);
                return null;
            }
        }
        else return null;
    }

    @Override
    public boolean mkdir(String path, String token) {
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
            String pk = jwtTokenProvider.getUserPk(token);
            Optional<UserEntity> userEntity = service.findById(pk);
            String uuid = UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)).toString();
            FileServerPrivateDto dto = new FileServerPrivateDto(
                    path, // file path (need to change)
                    name, // file name
                    uuid, // file name to change UUID
                    "dir",
                    0, // file size(KB)
                    userEntity.get().getName(),
                    location.toString(), // file folder path (need to change)
                    0,
                    0
            );
            repository.save(new FileServerPrivateEntity(dto));
            logComponent.sendLog("Cloud", "[mkdir(private)] mkdir dto : "+dto, true, TOPIC_CLOUD_LOG);
            return true;
        }
        else{
            logComponent.sendLog("Cloud", "[mkdir(private)] failed to mkdir (path) : "+path, false, TOPIC_CLOUD_LOG);
            return false;
        }
    }

    @Override
    public boolean existsByPath(String path) {
//        String originPath = changeUnderBarToSeparator(path);
        boolean result = repository.existsByPath(path);
        return result;
    }

    @Override
    public long deleteByPath(String path, String accessToken) {
        FileServerPrivateEntity entity = repository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[deleteByPath(private)] file entity is null (path) : "+path, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            // json type { file : origin file path, path : destination to move file }
            String jsonResult = encodingJSON("delete", "delete", entity.getUuid(), entity.getPath(), entity.getLocation());
            // kafka send
            producer.sendCloudMessage(jsonResult);
            logComponent.sendLog("Cloud", "[deleteByPath(private)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            logComponent.sendLog("Cloud", "[deleteByPath(private)] user info doesn't exist (path) : "+path, false, TOPIC_CLOUD_LOG);
            return -1;
        }
    }

    @Override
    public int moveFile(String uuid, String location, String accessToken) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[moveFile(private)] file entity is null (uuid) : "+uuid+", location : " + location, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        String filePath = entity.getPath();
        String movePath = location+entity.getName();

        // json type { file : origin file path, path : destination to move file }
        String jsonResult = encodingJSON("move", "move", uuid, filePath, movePath);
        System.out.println("deleteByPath : " + jsonResult);
        // kafka send
        producer.sendCloudMessage(jsonResult);
        logComponent.sendLog("Cloud", "[moveFile(private)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
        return 0;
    }

    @Override
    public int moveTrash(String uuid, String accessToken) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[moveTrash(private)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }

        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            String jsonResult = encodingJSON("move", "delete", entity.getUuid(), entity.getPath(), entity.getLocation());
            System.out.println("deleteByPath : " + jsonResult);
            // kafka send
            producer.sendCloudMessage(jsonResult);
            logComponent.sendLog("Cloud", "[moveTrash(private)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            logComponent.sendLog("Cloud", "[moveTrash(private)] file doesn't exist (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }
    }

    @Override
    public int restore(String uuid, String accessToken) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            logComponent.sendLog("Cloud", "[restore(private)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            String jsonResult = encodingJSON("move", "restore", entity.getUuid(), entity.getPath(), entity.getLocation());
            System.out.println("deleteByPath : " + jsonResult);
            // kafka send
            producer.sendMessage(jsonResult);
            logComponent.sendLog("Cloud", "[restore(private)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            logComponent.sendLog("Cloud", "[restore(private)] file doesn't exist (accessToken) : "+accessToken, false, TOPIC_CLOUD_LOG);
            return -1;
        }
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
    public int updateByFileServerPrivateEntity(FileServerPrivateEntity entity) {
        if(existsByPath(entity.getPath())){
            logComponent.sendLog("Cloud", "[updateByFileServerPrivateEntity(private)] already exist file in location (path): "+entity.getPath(), false, TOPIC_CLOUD_LOG);
            return -1;
        }
        else{
            FileServerPrivateEntity resultEntity = repository.save(entity);
            if(ObjectUtils.isEmpty(resultEntity)){ // error during change info on DB
                logComponent.sendLog("Cloud", "[updateByFileServerPrivateEntity(private)] Error during change info on DB (uuid): "+entity.getUuid(), false, TOPIC_CLOUD_LOG);
                return -2;
            }
            else{ // success update file info
                logComponent.sendLog("Cloud", "[updateByFileServerPrivateEntity(private)] change info on DB (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_LOG);
                return 0;
            }
        }
    }

    @Override
    public boolean save(FileServerPrivateEntity entity) {
        return !ObjectUtils.isEmpty(repository.save(entity));
    }

    @Transactional
    @Override
    public void privateFileCheck() {
        List<UserEntity> userList = service.findAll();
        File defaultPath = new File(diskPath);
        File[] files = defaultPath.listFiles();
        if(files != null) {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                String fileName = file.getName();
                sb.append(fileName).append("\n");
                for(UserEntity entity : userList){
                    if(entity.getId().equals(fileName)){
                        String owner = entity.getId();
                        filesWalk(diskPath+File.separator+owner, owner);
                        break;
                    }
                }
            }
            logComponent.sendLog("Cloud-Check", "[privateFileCheck(private)] file top list : "+sb.toString(), true, TOPIC_CLOUD_CHECK_LOG);
        }
        File trashDefaultPath = new File(trashPath);
        File[] trashFiles = trashDefaultPath.listFiles();
        if(trashFiles != null){
            for(File file : trashFiles){
                String fileName = file.getName();
                for(UserEntity entity : userList){
                    if(entity.getId().equals(fileName)){
                        String owner = entity.getId();
                        filesWalkTrash(trashPath+File.separator+owner, owner);
                        break;
                    }
                }
            }
        }
        deleteThumbNail();
    }
    @Override
    public void filesWalk(String pathUrl, String owner){
        Path originPath = Paths.get(pathUrl);
        List<Path> pathList;
        try{
            Stream<Path> pathStream = Files.walk(originPath);
            pathList = pathStream.collect(Collectors.toList());
            List<FileServerPrivateDto> fileList = new ArrayList<>();
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
                    fileList.add(new FileServerPrivateDto(
                            tmpPath,
                            file.getName(),
                            uuid,
                            extension,
                            (float) (file.length() / 1024),
                            owner,
                            tmpLocation,
                            1,
                            0
                    ));
                    if (Arrays.asList(videoExtensionList).contains(extension) && !thumbNailRepository.existsByUuid(uuid)) {
                        mediaFileList.add(file);
                    }
                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            fileServerCustomRepository.saveBatchPrivate(fileList);
            for(File file : mediaFileList){
                thumbNailService.makeThumbNail(file,
                        UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString(),
                        "private"
                );
            }
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[filesWalk(private)] file check error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
    }
    @Transactional
    @Override
    public void filesWalkTrash(String pathUrl, String owner){
        List<FileServerPrivateTrashEntity> list = trashRepository.findAll();
        Path originPath = Paths.get(pathUrl);
        List<Path> pathList;
        try {
            Stream<Path> pathStream = Files.walk(originPath);
            pathList = pathStream.collect(Collectors.toList());
            List<FileServerPrivateTrashEntity> existFileList = new ArrayList<>();
            List<FileServerPrivateTrashEntity> newFileList = new ArrayList<>();
            for(Path path : pathList){
                File file = path.toFile();
                try {
                    String tmpPath = commonService.changeSeparatorToUnderBar(file.getPath());
                    String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();
                    List<FileServerPrivateTrashEntity> filterList = list.stream()
                            .filter(entity -> uuid.equals(entity.getUuid()))
                            .toList();
                    if(filterList.isEmpty()){
                        String tmpLocation = commonService.changeSeparatorToUnderBar(file.getPath().split(file.getName())[0]);
                        String extension = "dir";
                        if(!file.isDirectory()) {
                            extension = file.getName().substring(file.getName().lastIndexOf(".") + 1); // file type (need to check ex: txt file -> text/plan)
                        }
                        //int id, String uuid, String path, String originPath, String name, String type, float size, String owner, String location, int state
                        newFileList.add(new FileServerPrivateTrashEntity(
                                0,
                                uuid,
                                tmpPath,
                                commonService.changeSeparatorToUnderBar(diskPath),
                                file.getName(),
                                extension,
                                (float) (file.length() / 1024),
                                owner,
                                tmpLocation,
                                0
                        ));
                    }
                    else{
                        existFileList.addAll(filterList);
                    }

                }
                catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            for(FileServerPrivateTrashEntity entity : existFileList){
                list.remove(entity);
            }
            trashRepository.deleteAll(list);
            trashRepository.saveAll(newFileList);
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[filesWalkTrash(private)] file check error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
    }
    @Transactional
    @Override
    public void deleteThumbNail(){
        List<FileServerThumbNailEntity> thumbNailEntityList = thumbNailRepository.findAllNotInPrivate();
        for(FileServerThumbNailEntity entity : thumbNailEntityList){
            File out = new File(commonService.changeUnderBarToSeparator(entity.getPath()));
            if(out.exists()){
                if(out.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(private)] files is deleted (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_CHECK_LOG);
                }
                else{
                    logComponent.sendLog("Cloud-Check", "[deleteThumbNail(private)] files is doesn't delete (uuid) : "+entity.getUuid(), false, TOPIC_CLOUD_CHECK_LOG);
                }
            }
        }
    }
}
