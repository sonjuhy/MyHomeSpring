package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerPrivateRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
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
public class FileServerPrivateServiceImpl implements FileServerPrivateService {

//    private final String diskPath = "/home/disk1/home/private";
    private final String diskPath;
    private final String trashPath;
    private final String thumbnailPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";
    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    @Autowired
    KafkaProducer producer;

    @Autowired
    FileServerPrivateRepository repository;

    @Autowired
    UserService service;

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;

    @Autowired
    FileServerThumbNailService thumbNailService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    public FileServerPrivateServiceImpl(FileDefaultPathRepository fileDefaultPathRepository){
        FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");
        FileDefaultPathEntity trashEntity = fileDefaultPathRepository.findByPathName("trash");
        FileDefaultPathEntity thumbnailEntity = fileDefaultPathRepository.findByPathName("thumbnail");
        diskPath = changeUnderBarToSeparator(storeEntity.getPrivateDefaultPath());
        trashPath = changeUnderBarToSeparator(trashEntity.getPrivateDefaultPath());
        thumbnailPath = changeUnderBarToSeparator(thumbnailEntity.getPrivateDefaultPath());

        producer.sendLogDto("Cloud",
                "[FileServerPrivateServiceImpl] diskPath : "+diskPath+", trashPath : "+trashPath+", thumbnailPath : " + thumbnailPath,
                true,
                TOPIC_CLOUD_LOG);

    }

    @Override
    public FileServerPrivateEntity findByPath(String path) {
        String originPath = changeUnderBarToSeparator(path);
        FileServerPrivateEntity entity = repository.findByPath(originPath);
        return entity;
    }

    @Override
    public FileServerPrivateEntity findByUuid(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        return entity;
    }

    @Override
    public List<FileServerPrivateEntity> findByLocation(String location, int mode) {
        List<FileServerPrivateEntity> list = repository.findByLocationAndDelete(location, mode);
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
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, contentType);
        return httpHeaders;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String path, String token, Model model) {
        boolean result = jwtTokenProvider.validateToken(token);
        if(result){
            String id = jwtTokenProvider.getUserPk(token);
            Optional<UserEntity> userEntity = service.findById(id);
//            String fileLocation = defaultUploadPath+File.separator+path+File.separator;
            String fileLocation = changeUnderBarToSeparator(path);
            List<FileServerPrivateEntity> list = new ArrayList<>();
            for(MultipartFile file : files){
                if(!file.isEmpty()){
                    try{
                        FileServerPrivateDto dto = new FileServerPrivateDto(
                                fileLocation+file.getOriginalFilename(), // file path (need to change)
                                file.getOriginalFilename(), // file name
                                UUID.randomUUID().toString(), // file name to change UUID
                                Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1), // file type (need to check ex: txt file -> text/plan)
                                (float)file.getSize(), // file size(KB)
                                userEntity.get().getName(),
                                fileLocation, // file folder path (need to change)
                                0,
                                0
                        );
                        System.out.println(file.getResource());
                        list.add(new FileServerPrivateEntity(dto));
                        String saveName = fileLocation+dto.getName();
                        Path savePath = Paths.get(saveName);
                        file.transferTo(savePath);
                    } catch (IOException e) {
                        e.printStackTrace();
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
            producer.sendLogDto("Cloud", "[uploadFiles(private)] file size : "+resultArr.size(), true, TOPIC_CLOUD_LOG);
            return resultArr;
        }
        else{
            producer.sendLogDto("Cloud", "[uploadFiles(private)] jwtToken validate failed", false, TOPIC_CLOUD_LOG);
        }
        return null;
    }

    @Override
    public boolean mkdir(String path, String token) {
        String originPath = changeUnderBarToSeparator(path);
        File file = new File(originPath);
        if(file.mkdir()){
            String[] paths = originPath.split(File.separator);
            String name = paths[paths.length-1];
            StringBuilder location = new StringBuilder();
            for(int i=0;i<paths.length-1;i++){
                location.append(paths[i]).append(File.separator);
            }
            String pk = jwtTokenProvider.getUserPk(token);
            Optional<UserEntity> userEntity = service.findById(pk);

            FileServerPrivateDto dto = new FileServerPrivateDto(
                    originPath, // file path (need to change)
                    name, // file name
                    UUID.randomUUID().toString(), // file name to change UUID
                    "dir",
                    0, // file size(KB)
                    userEntity.get().getName(),
                    location.toString(), // file folder path (need to change)
                    0,
                    0
            );
            repository.save(new FileServerPrivateEntity(dto));
            producer.sendLogDto("Cloud", "[mkdir(private)] mkdir dto : "+dto, true, TOPIC_CLOUD_LOG);
            return true;
        }
        else{
            producer.sendLogDto("Cloud", "[mkdir(private)] failed to mkdir (path) : "+path, false, TOPIC_CLOUD_LOG);
            return false;
        }
    }

    @Override
    public boolean existsByPath(String path) {
        String originPath = changeUnderBarToSeparator(path);
        boolean result = repository.existsByPath(originPath);
        return result;
    }

    @Override
    public long deleteByPath(String path, String accessToken) { // add owner
        String originPath = changeUnderBarToSeparator(path);
        FileServerPrivateEntity entity = repository.findByPath(originPath);
        if(ObjectUtils.isEmpty(entity)){
            producer.sendLogDto("Cloud", "[deleteByPath(private)] file entity is null (path) : "+path, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            // json type { file : origin file path, path : destination to move file }
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purpose", "delete");
            jsonObject.addProperty("action", "delete");
            jsonObject.addProperty("uuid", entity.getUuid());
            jsonObject.addProperty("file", entity.getPath());
            jsonObject.addProperty("path", trashPath+File.separator+entityUser.get().getUserId()+File.separator+entity.getName());
            String jsonResult = gson.toJson(jsonObject);
            // kafka send
            producer.sendMessage(jsonResult);
            producer.sendLogDto("Cloud", "[deleteByPath(private)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            producer.sendLogDto("Cloud", "[deleteByPath(private)] file doesn't exist (path) : "+path, false, TOPIC_CLOUD_LOG);
            return -1;
        }
    }

    @Override
    public int moveFile(String uuid, String location, String accessToken) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            producer.sendLogDto("Cloud", "[moveFile(private)] file entity is null (uuid) : "+uuid+", location : " + location, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        String filePath = entity.getPath();
        String movePath = location+entity.getName();

//        Optional<UserEntity> entityUser = service.findById(accessToken);

        // json type { file : origin file path, path : destination to move file }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("purpose", "delete");
        jsonObject.addProperty("action", "delete");
        jsonObject.addProperty("uuid", uuid);
        jsonObject.addProperty("file", filePath);
        jsonObject.addProperty("path", movePath);
        String jsonResult = gson.toJson(jsonObject);
        System.out.println("deleteByPath : " + jsonResult);
        // kafka send
        producer.sendMessage(jsonResult);
        producer.sendLogDto("Cloud", "[moveFile(private)] json : "+jsonResult, true, TOPIC_CLOUD_LOG);
        return 0;
    }

    @Override
    public int moveTrash(String uuid, String accessToken) {
        // Will change to send Kafka
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            producer.sendLogDto("Cloud", "[moveTrash(private)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purpose", "delete");
            jsonObject.addProperty("action", "delete");
            jsonObject.addProperty("uuid", entity.getUuid());
            jsonObject.addProperty("file", trashPath+File.separator+entityUser.get().getUserId()+File.separator+entity.getName());
            jsonObject.addProperty("path", entity.getPath());
            String jsonResult = gson.toJson(jsonObject);
            System.out.println("deleteByPath : " + jsonResult);
            // kafka send
            producer.sendMessage(jsonResult);
            producer.sendLogDto("Cloud", "[moveTrash(private)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            producer.sendLogDto("Cloud", "[moveTrash(private)] file doesn't exist (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }
    }

    @Override
    public int restore(String uuid, String accessToken) {
        // Will change to send Kafka
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            producer.sendLogDto("Cloud", "[restore(private)] file entity is null (uuid) : "+uuid, false, TOPIC_CLOUD_LOG);
            return -1;
        }
        Optional<UserEntity> entityUser = service.findById(accessToken);
        if(entityUser.isPresent()){
            Gson gson = new Gson();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("purpose", "delete");
            jsonObject.addProperty("action", "delete");
            jsonObject.addProperty("uuid", entity.getUuid());
            jsonObject.addProperty("file", trashPath+File.separator+entityUser.get().getUserId()+File.separator+entity.getName());
            jsonObject.addProperty("path", entity.getPath());
            String jsonResult = gson.toJson(jsonObject);
            System.out.println("deleteByPath : " + jsonResult);
            // kafka send
            producer.sendMessage(jsonResult);
            producer.sendLogDto("Cloud", "[restore(private)] file info send to django (json) : "+jsonResult, true, TOPIC_CLOUD_LOG);
            return 0;
        }
        else{
            producer.sendLogDto("Cloud", "[restore(private)] file doesn't exist (accessToken) : "+accessToken, false, TOPIC_CLOUD_LOG);
            return -1;
        }
    }

    @Override
    public int updateByFileServerPrivateEntity(FileServerPrivateEntity entity) {
        if(existsByPath(entity.getPath())){
            producer.sendLogDto("Cloud", "[updateByFileServerPrivateEntity(private)] already exist file in location (path): "+entity.getPath(), false, TOPIC_CLOUD_LOG);
            return -1;
        }
        else{
            FileServerPrivateEntity resultEntity = repository.save(entity);
            if(ObjectUtils.isEmpty(resultEntity)){ // error during change info on DB
                producer.sendLogDto("Cloud", "[updateByFileServerPrivateEntity(private)] Error during change info on DB (uuid): "+entity.getUuid(), false, TOPIC_CLOUD_LOG);
                return -2;
            }
            else{ // success update file info
                producer.sendLogDto("Cloud", "[updateByFileServerPrivateEntity(private)] change info on DB (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_LOG);
                return 0;
            }
        }
    }

    @Override
    public boolean save(FileServerPrivateEntity entity) {
        return !ObjectUtils.isEmpty(repository.save(entity));
    }

    @Override
    public void privateFileCheck() {
        List<UserEntity> userList = service.findAll();
        repository.updateAllStateToOne();
        traversalFolder(diskPath, userList);
        deleteThumbNail();
        repository.deleteByState(0);
        repository.updateAllStateToZero();
    }
    private String changeUnderBarToSeparator(String path){
        return path.replaceAll("_", Matcher.quoteReplacement(File.separator));
    }
    private String changeSeparatorToUnderBar(String path){
        return path.replaceAll(Matcher.quoteReplacement(File.separator), "_");
    }

    private void traversalFolder(String path, List<UserEntity> userList){
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(files == null){
            ArrayList<String> dirList = new ArrayList<>();
            for(File file : files){
                String type, extension;
                if(file.isDirectory()) {
                    type = "dir : ";
                    extension = "dir";
                    dirList.add(file.getName());
                }
                else {
                    type = "file : ";
                    extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
                }
                FileServerPrivateEntity entity = repository.findByPath(file.getPath());

                if(entity == null){
                    String folderName = file.getPath().split(diskPath)[1].split(File.separator)[0];
                    int userNum = Integer.parseInt(folderName.split("_")[1]);
                    String owner = "owner";
                    for(UserEntity user : userList){
                        if(user.getUserId() == userNum){
                            owner = user.getName();
                            break;
                        }
                    }
                    String tmpPath = changeSeparatorToUnderBar(file.getPath()), tmpLocation = changeSeparatorToUnderBar(file.getPath().split(file.getName())[0]);
                    String uuid = UUID.nameUUIDFromBytes(tmpPath.getBytes(StandardCharsets.UTF_8)).toString();
//                if(tmpPath.contains("trash")){
//                    tmpPath = tmpPath.replace("trash", "");
//                }
//                if(tmpLocation.contains("trash")){
//                    tmpLocation = tmpLocation.replace("trash", "");
//                }
                    FileServerPrivateDto dto = new FileServerPrivateDto(
                            tmpPath, // file path (need to change)
                            file.getName(), // file name
                            uuid, // file name to change UUID
                            extension, // file type (need to check ex: txt file -> text/plan)
                            (float)(file.length()/1024), // file size(KB)
                            owner,
                            tmpLocation, // file folder path (need to change)
                            1,
                            0
                    );
                    repository.save(new FileServerPrivateEntity(dto));
                    if(Arrays.asList(videoExtensionList).contains(extension)){
                        try {
                            Path source = Paths.get(file.getPath());
                            System.out.println(Files.probeContentType(source));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        thumbNailService.makeThumbNail(file, uuid, "private");
                    }
                    producer.sendLogDto("Cloud-Check", "[traversalFolder(private)] file (dto) : "+dto+", no exist file", true, TOPIC_CLOUD_CHECK_LOG);
                }
                else{
                    producer.sendLogDto("Cloud-Check", "[traversalFolder(private)] file (path) : "+file.getPath()+", (name) : "+type+file.getName()+", exist file", true, TOPIC_CLOUD_CHECK_LOG);
                }

            }
            for(String folder : dirList){
                traversalFolder(path+File.separator+folder, userList);
            }
        }
        else{
            producer.sendLogDto("Cloud-Check", "[traversalFolder(private)] files is null (path) : "+path, false, TOPIC_CLOUD_CHECK_LOG);
        }
    }
    private void deleteThumbNail(){
        List<FileServerThumbNailEntity> thumbNailEntityList = thumbNailRepository.findAllNotInPublic();
        for(FileServerThumbNailEntity entity : thumbNailEntityList){
            File out = new File(changeUnderBarToSeparator(entity.getPath()));
            if(out.exists()){
                if(out.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
                    producer.sendLogDto("Cloud-Check", "[deleteThumbNail(private)] files is deleted (uuid) : "+entity.getUuid(), true, TOPIC_CLOUD_CHECK_LOG);
                }
                else{
                    producer.sendLogDto("Cloud-Check", "[deleteThumbNail(private)] files is doesn't delete (uuid) : "+entity.getUuid(), false, TOPIC_CLOUD_CHECK_LOG);
                }
            }
        }
    }
}
