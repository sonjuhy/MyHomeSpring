package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPrivateTrashDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.*;
import com.myhome.server.db.repository.FileServerPrivateRepository;
import com.myhome.server.db.repository.FileServerPrivateTrashRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.apache.catalina.User;
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

@Service
public class FileServerPrivateServiceImpl implements FileServerPrivateService {

//    private final String diskPath = "/home/disk1/home/private";
    private final String diskPath = "C:\\\\Users\\\\SonJunHyeok\\\\Desktop\\\\test\\\\private\\\\";

    private final String[] videoExtensionList = {"mp4", "avi", "mov", "wmv", "avchd", "webm", "mpeg4"};

    @Value("${part4.upload.path}")
    private String defaultUploadPath;

    @Autowired
    FileServerPrivateRepository repository;

    @Autowired
    FileServerPrivateTrashRepository trashRepository;

    @Autowired
    UserService service = new UserServiceImpl();

    @Autowired
    FileServerThumbNailRepository thumbNailRepository;

    @Autowired
    FileServerThumbNailService thumbNailService = new FileServerThumbNailServiceImpl();

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Override
    public FileServerPrivateEntity findByPath(String path) {
        FileServerPrivateEntity entity = repository.findByPath(path);
        return entity;
    }

    @Override
    public FileServerPrivateEntity findByUuid(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        return entity;
    }

    @Override
    public List<FileServerPrivateEntity> findByLocation(String location) {
        List<FileServerPrivateEntity> list = repository.findByLocation(location);
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
            String fileLocation = path;
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
            return resultArr;
        }
        return null;
    }

    @Override
    public boolean existsByPath(String path) {
        boolean result = repository.existsByPath(path);
        return result;
    }

    @Override
    public long deleteByPath(String path) { // add owner
        FileServerPrivateEntity entity = repository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt"; // test filePath
        String filePath = testInPath;
        //        String filePath = entity.getPath();

        File file = new File(filePath);

        if(file.exists()){ // check file exist
            if(file.delete()){ // if file exist, delete file
                System.out.println("delete success");
            }
            else{
                System.out.println("delete failed");
            }
        }
        long result = repository.deleteByPath(path); // delete file info from DB
        return result;
    }

    @Override
    public int moveFile(String uuid, String location) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
//        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt"; // test filePath
//        String testOutPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\a.txt"; // test move location
//        String filePath = testInPath;
//        String movePath = testOutPath;
        String filePath = entity.getPath();
        String movePath = location+entity.getName();

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
//        int result = repository.updateLocation(path, location); // update file location info from DB
        entity.changePathAndLocation(movePath, location); // Dirty check
        return 0;
    }

    @Override
    public int moveTrash(String uuid) {
        FileServerPrivateEntity entity = repository.findByUuid(uuid);
        if(entity != null){
            FileServerPrivateTrashDto dto = new FileServerPrivateTrashDto(entity);
            FileServerPrivateTrashEntity trashEntity = new FileServerPrivateTrashEntity(dto);
            trashRepository.save(trashEntity);

            String trashPath = diskPath+"trash"+File.separator+dto.getName();
            try{
                File in = new File(entity.getPath());
                File out = new File(trashPath);
                FileCopyUtils.copy(in, out); // copy file from origin location to new location
                if(in.exists()){ // check origin file exist
                    if(in.delete()){ // if file exist
                        System.out.println("delete success");
                        repository.deleteByPath(entity.getPath());
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
        return -1;
    }

    @Override
    public int restore(String uuid) {
        FileServerPrivateTrashEntity trashEntity = trashRepository.findByUuid(uuid);
        if(trashEntity != null){
            FileServerPrivateDto dto = new FileServerPrivateDto(
                    trashEntity.getPath(),
                    trashEntity.getName(),
                    trashEntity.getUuid(),
                    trashEntity.getType(),
                    trashEntity.getSize(),
                    trashEntity.getOwner(),
                    trashEntity.getLocation(),
                    trashEntity.getState()
            );
            repository.save(new FileServerPrivateEntity(dto));
            String trashPath = diskPath+"trash\\"+dto.getName();
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
        return -1;
    }

    @Override
    public int updateByFileServerPublicEntity(FileServerPrivateEntity entity) {
        if(existsByPath(entity.getPath())){
            System.out.println("already exist file in location");
            return -1;
        }
        else{
            FileServerPrivateEntity resultEntity = repository.save(entity);
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
    private void traversalFolder(String path, List<UserEntity> userList){
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
                extension = file.getName().substring(file.getName().lastIndexOf(".")+1);
            }
            FileServerPrivateEntity entity = repository.findByPath(file.getPath());
            if(entity == null){
                String folderName = file.getPath().split(diskPath)[1].split("\\\\")[0];
                int userNum = Integer.parseInt(folderName.split("_")[1]);
                System.out.println("FileServerPrivateServiceImpl : " + "\\\\"+", "+File.separator);
                String owner = "owner";
                for(UserEntity user : userList){
                    if(user.getUserId() == userNum){
                        owner = user.getName();
                        break;
                    }
                }
                String uuid = UUID.randomUUID().toString();
                FileServerPrivateDto dto = new FileServerPrivateDto(
                        file.getPath(), // file path (need to change)
                        file.getName(), // file name
                        uuid, // file name to change UUID
                        extension, // file type (need to check ex: txt file -> text/plan)
                        (float)(file.length()/1024), // file size(KB)
                        owner,
                        file.getPath().split(file.getName())[0], // file folder path (need to change)
                        1
                );
                repository.save(new FileServerPrivateEntity(dto));
                if(Arrays.asList(videoExtensionList).contains(extension)){
                    try {
                        Path source = Paths.get(file.getPath());
                        System.out.println(Files.probeContentType(source));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    thumbNailService.makeThumbNail(file, uuid);
                }
            }
            System.out.println(file.getPath()+", "+type+file.getName());
        }
        for(String folder : dirList){
            traversalFolder(path+File.separator+folder, userList);
        }
    }
    private void deleteThumbNail(){
        List<FileServerPrivateEntity> list = repository.findByState(0);
        for(FileServerPrivateEntity entity : list){
            File out = new File(entity.getPath());
            if(out.exists()){
                if(out.delete()){
                    thumbNailRepository.deleteByUuid(entity.getUuid());
                }
            }
        }
    }
}
