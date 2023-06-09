package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.dto.FileServerPublicTrashDto;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileServerPublicRepository;
import com.myhome.server.db.repository.FileServerPublicTrashRepository;
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
    FileServerPublicTrashRepository trashRepository;

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
    public List<FileServerPublicTrashEntity> findTrashAll() {
        List<FileServerPublicTrashEntity> list = trashRepository.findAll();
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
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt"; // test filePath
        String filePath = testInPath;

//        File file = new File(filePath);
        File file = new File(path);

        if(file.exists()){ // check file exist
            if(file.isDirectory()){
                deleteFolder(file.getPath());
            }
            else{
                if(file.delete()){ // if file exist, delete file
                    fileServerRepository.deleteByPath(filePath);
                    System.out.println("delete success");
                }
                else{
                    System.out.println("delete failed");
                }
            }
        }
        return 0;
    }
    private void deleteFolder(String path){
        File dir = new File(path);
        File[] files = dir.listFiles();
        FileServerPublicTrashEntity entity;
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    deleteFolder(file.getPath());
                    if(file.delete()){
                        entity = trashRepository.findByPath(path);
                        if(entity != null) trashRepository.delete(trashRepository.findByPath(path));
                        System.out.println("FileServerPublicServiceImpl delete folder Success");
                    }
                    else{
                        System.out.println("FileServerPublicServiceImpl delete folder failed");
                    }
                }
                else{
                    if(file.delete()){
                        entity = trashRepository.findByPath(path);
                        if(entity != null) trashRepository.delete(trashRepository.findByPath(path));
                        System.out.println("FileServerPublicServiceImpl delete file Success");
                    }
                    else{
                        System.out.println("FileServerPublicServiceImpl delete file failed");
                    }
                }
            }
        }
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

    @Transactional
    @Override
    public int moveTrash(String uuid) {
        FileServerPublicEntity entity = fileServerRepository.findByUuid(uuid);
        if(entity != null){

            FileServerPublicTrashDto dto = new FileServerPublicTrashDto(entity);
            FileServerPublicTrashEntity trashEntity = new FileServerPublicTrashEntity(dto);
            trashRepository.save(trashEntity);
            String tmpTrashPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\trash\\" + trashEntity.getName();
            //            String trashPath = trashPath+"trash";

            if(entity.getType().equals("dir")){
                moveFolder(entity.getPath(), tmpTrashPath);
            }
            else{
                try{
                    File in = new File(entity.getPath());
                    File out = new File(tmpTrashPath);
                    FileCopyUtils.copy(in, out); // copy file from origin location to new location
                    if(in.exists()){ // check origin file exist
                        if(in.delete()){ // if file exist
                            System.out.println("delete success");
                            fileServerRepository.deleteByPath(entity.getPath());
                            return 0;
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
            }
        }
        return -1;
    }
    private void moveFolder(String path, String destPath){
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory()) {
                    moveFolder(path+File.separator+file.getName(), destPath+File.separator+file.getName());
                    String dirName = file.getName();
                    FileServerPublicEntity entity = fileServerRepository.findByPath(path+File.separator+dirName);
                    if(entity != null){
                        FileServerPublicTrashDto trashDto = new FileServerPublicTrashDto(entity);
                        FileServerPublicTrashEntity trashEntity = new FileServerPublicTrashEntity(trashDto);
                        trashRepository.save(trashEntity);
                    }
                    else{
                        FileServerPublicTrashDto trashDto = new FileServerPublicTrashDto();
                        trashDto.setPath(path+File.separator+file.getName());
                        trashDto.setLocation(path);
                        trashDto.setName(file.getName());
                        trashDto.setSize(0);
                        trashDto.setState(0);
                        trashDto.setType("dir");
                        trashDto.setUuidName(UUID.randomUUID().toString());
                        trashRepository.save(new FileServerPublicTrashEntity(trashDto));
                    }
                    if(file.delete()){
                        System.out.println("FileServerPublicService Folder delete Success : "+file.getPath());
                    }
                    else{
                        System.out.println("FileServerPublicService Folder delete Fail : "+file.getPath());
                    }
                }
                else {
                    try{
                        FileServerPublicEntity entity = fileServerRepository.findByPath(path);
                        File in = new File(entity.getPath());
                        File out = new File(destPath);
                        FileCopyUtils.copy(in, out); // copy file from origin location to new location
                        if(in.exists()){ // check origin file exist
                            if(in.delete()){ // if file exist
                                System.out.println("delete success");
                                fileServerRepository.deleteByPath(entity.getPath());
                            }
                            else{
                                System.out.println("delete failed");
                            }
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public int restore(String uuid) {
        FileServerPublicTrashEntity trashEntity = trashRepository.findByUuid(uuid);

        if(trashEntity != null){
            String tmpTrashPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\trash\\"+trashEntity.getName();
            String trashPath = tmpTrashPath;
//            String trashPath = diskPath+"trash";
            if(trashEntity.getType().equals("dir")){
                moveFolder(trashPath, trashEntity.getPath());
            }
            else{
                FileServerPublicDto dto = new FileServerPublicDto(
                        trashEntity.getPath(),
                        trashEntity.getName(),
                        trashEntity.getUuid(),
                        trashEntity.getType(),
                        trashEntity.getSize(),
                        trashEntity.getLocation(),
                        trashEntity.getState()
                );
                fileServerRepository.save(new FileServerPublicEntity(dto));

                try{
                    File in = new File(trashPath);
                    File out = new File(dto.getPath());
                    FileCopyUtils.copy(in, out); // copy file from origin location to new location
                    if(in.exists()){ // check origin file exist
                        if(in.delete()){ // if file exist
                            System.out.println("delete success");
                            trashRepository.deleteByUuid(dto.getUuidName());
                            return 0;
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
            }
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
