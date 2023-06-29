package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileServerPublicRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class FileServerPublicServiceImpl implements FileServerPublicService {

//    private final String diskPath = "/home/disk1/home/public";
//    private final String trashPath = "/home/disk1/home/public/휴지통";

    private final String diskPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\public";
    private final String trashPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\trash";

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    @Autowired
    KafkaProducer producer;

    @Autowired
    FileServerPublicRepository fileServerRepository;

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;

    @Autowired
    FileServerThumbNailService thumbNailService = new FileServerThumbNailServiceImpl();

    @Override
    public FileServerPublicEntity findByPath(String path) {
        if("default".equals(path)) path = diskPath;
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        return entity;
    }

    @Override
    public FileServerPublicEntity findByUuidName(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        return entity;
    }

    @Override
    public List<FileServerPublicEntity> findByLocation(String location, int mode) {
        System.out.println("location : " + location);
        if("default".equals(location)) location = diskPath;
        List<FileServerPublicEntity> list = fileServerRepository.findByLocationAndDelete(location, mode);
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

        return null;
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files, String path, Model model) {

//        String fileLocation = defaultUploadPath+File.separator+path+File.separator;
        if(path != null && path.isBlank() && !path.isEmpty()) {
            System.out.println("uploadFile impl here : " + path);
            path += File.separator;
        }
        else{
            System.out.println("uploadFile impl path is empty");
        }
        String fileLocation = "C:\\Users\\SonJunHyeok\\Desktop\\test\\public"+File.separator+path;
        List<FileServerPublicEntity> list = new ArrayList<>();
        for(MultipartFile file : files){
            if(!file.isEmpty()){
                System.out.println("uploadFile impl filepath : " + fileLocation+file.getOriginalFilename());
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
                    e.printStackTrace();
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
        return resultArr;
    }

    @Override
    public void mkdir(String path) {
        File file = new File(path);
        file.mkdir();
        System.out.println("Public mkdir : " + path);
//        String[] paths = path.split(File.separator);
        String[] paths = path.split("\\\\");
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
    }

    @Override
    public boolean existsByPath(String path) {
        boolean result = fileServerRepository.existsByPath(path);
        return result;
    }

    @Override
    public long deleteByPath(String path) {
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
        // json type { file : origin file path, path : destination to move file }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("purpose", "delete");
        jsonObject.addProperty("action", "delete");
        jsonObject.addProperty("uuid", entity.getUuid());
        jsonObject.addProperty("file", trashPath+"\\"+entity.getName());
        jsonObject.addProperty("path", entity.getPath());
        String jsonResult = gson.toJson(jsonObject);
        System.out.println("deleteByPath : " + jsonResult);
        // kafka send
        producer.sendMessage(jsonResult);
        return 0;
    }

    @Override
    public int moveFile(String path, String location) {
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
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
        }
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
            jsonObject.addProperty("file", trashPath+"\\"+entity.getName());
            jsonObject.addProperty("path", entity.getPath());
            String jsonResult = gson.toJson(jsonObject);
            // kafka send
            producer.sendMessage(jsonResult);
        }
        return -1;
    }

    @Override
    public int updateByFileServerPublicEntity(FileServerPublicEntity entity) {
        if(existsByPath(entity.getPath())){
            System.out.println("already exist file in location");
            return -1;
        }
        else{
            FileServerPublicEntity resultEntity = fileServerRepository.save(entity);
            if(ObjectUtils.isEmpty(resultEntity)){ // error during change info on DB
                System.out.println("Error during change info on DB");
                return -2;
            }
            else{ // success update file info
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
        String tmpPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\public\\";
        String tmpTrashPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\trash\\";
        fileServerRepository.updateAllStateToOne();
        traversalFolder(tmpPath, true);
        traversalFolder(tmpTrashPath, false);
        deleteThumbNail();
        fileServerRepository.deleteByState(0);
    }

    private void traversalFolder(String path, boolean mode){
        System.out.println("This is Path : " + path);
        File dir = new File(path);
        File[] files = dir.listFiles();
        assert files != null;
        ArrayList<String> dirList = new ArrayList<>();
        System.out.println("Files size : " + files.length);
        for(File file : files){
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
            FileServerPublicEntity entity = fileServerRepository.findByPath(file.getPath());
            int deleteStatus = 0;
            String tmpPath = file.getPath(), tmpLocation = file.getPath().split(file.getName())[0];
            if(!mode) {
                deleteStatus = 1;
            }
            if(entity == null){
                String uuid = UUID.randomUUID().toString();
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
                fileServerRepository.save(new FileServerPublicEntity(dto));
                if(Arrays.asList(videoExtensionList).contains(extension)){
                    thumbNailService.makeThumbNail(file, uuid);
                }
            }
            System.out.println(file.getPath()+", "+type+file.getName());
        }

        for(String folder : dirList){
            traversalFolder(path+File.separator+folder, mode);
        }

    }
    private void deleteThumbNail(){
        List<FileServerPublicEntity> list = fileServerRepository.findByState(0);
        for(FileServerPublicEntity entity : list){
            File out = new File(entity.getPath());
            if(out.exists()){
                if(out.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
                }
            }
        }
    }
}
