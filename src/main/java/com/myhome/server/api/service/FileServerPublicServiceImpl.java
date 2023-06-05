package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.db.entity.FileServerPublicEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
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

    private final String diskPath = "/home/disk1/home/public";
    private final String trashPath = "/home/disk1/home/public/휴지통";

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

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
    public List<FileServerPublicEntity> findByLocation(String location) {
        System.out.println("location : " + location);
        if("default".equals(location)) location = diskPath;
        List<FileServerPublicEntity> list = fileServerRepository.findByLocation(location);
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
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt"; // test filePath
        String filePath = testInPath;
        //        String filePath = entity.getPath();

        File file = new File(filePath);

        if(file.exists()){ // check file exist
            if(file.delete()){ // if file exist, delete file
                fileServerRepository.deleteByPath(filePath);
                System.out.println("delete success");
            }
            else{
                System.out.println("delete failed");
            }
        }
        long result = fileServerRepository.deleteByPath(path); // delete file info from DB
        return result;
    }

    @Transactional
    @Override
    public int moveFile(String path, String location) {
        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\a.txt"; // test filePath
        String testOutPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\test2\\a.txt"; // test move location
        String filePath = testInPath;
        String movePath = testOutPath;
//        String filePath = entity.getPath();
//        String movePath = location;

        try{
            File in = new File(filePath);
            File out = new File(movePath);
            FileCopyUtils.copy(in, out); // copy file from origin location to new location
            if(in.exists()){ // check origin file exist
                if(in.delete()){ // if file exist
                    System.out.println("delete success");
                }
                else{
                    System.out.println("delete failed");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        entity.changePathAndLocation(movePath, location); // Dirty check
        return 0;
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
        fileServerRepository.updateAllStateToOne();
        traversalFolder(tmpPath);
        deleteThumbNail();
        fileServerRepository.deleteByState(0);
    }

    private void traversalFolder(String path){
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
            if(entity == null){
                String uuid = UUID.randomUUID().toString();
                FileServerPublicDto dto = new FileServerPublicDto(
                        file.getPath(), // file path (need to change)
                        file.getName(), // file name
                        uuid, // file name to change UUID
                        extension,
                        (float)(file.length()/1024), // file size(KB)
                        file.getPath().split(file.getName())[0], // file folder path (need to change)
                        1
                );
                fileServerRepository.save(new FileServerPublicEntity(dto));
                if(Arrays.asList(videoExtensionList).contains(extension)){
                    thumbNailService.makeThumbNail(file, uuid);
                }
            }
            System.out.println(file.getPath()+", "+type+file.getName());
        }
        for(String folder : dirList){
            traversalFolder(path+File.separator+folder);
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
