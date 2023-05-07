package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.FileServerPublicEntity;
import com.myhome.server.db.repository.FileServerPrivateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileServerPrivateServiceImpl implements FileServerPrivateService {

    private final String diskPath = "/home/disk1/home/private";

    @Autowired
    FileServerPrivateRepository repository;

    @Override
    public FileServerPrivateEntity findByPath(String path) {
        FileServerPrivateEntity entity = repository.findByPath(path);
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
    public int moveFile(String path, String location) {
        FileServerPrivateEntity entity = repository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt"; // test filePath
        String testOutPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\a.txt"; // test move location
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
//        int result = repository.updateLocation(path, location); // update file location info from DB
        entity.changePathAndLocation(movePath, location); // Dirty check
        return 0;
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
        String tmpPath = "C:\\\\Users\\\\SonJunHyeok\\\\Desktop\\\\";
        repository.updateAllStateToOne();
        traversalFolder(tmpPath);
        repository.deleteByState(0);
    }
    private void traversalFolder(String path){
        System.out.println("This is Path : " + path);
        File dir = new File(path);
        File[] files = dir.listFiles();
        assert files != null;
        ArrayList<String> dirList = new ArrayList<>();
        System.out.println("Files size : " + files.length);
        for(File file : files){
            String type="";
            if(file.isDirectory()) {
                type = "dir : ";
                dirList.add(file.getName());
            }
            else {
                type = "file : ";
                FileServerPrivateEntity entity = repository.findByPath(file.getPath()+File.separator+file.getName());
                if(entity == null){
                    FileServerPrivateDto dto = new FileServerPrivateDto(
                            file.getPath()+file.getName(), // file path (need to change)
                            file.getName(), // file name
                            UUID.randomUUID().toString(), // file name to change UUID
                            file.getName().substring(file.getName().lastIndexOf(".") + 1), // file type (need to check ex: txt file -> text/plan)
                            (float)(file.length()/1024), // file size(KB)
                            "owner",
                            file.getPath(), // file folder path (need to change)
                            1
                    );
                    repository.save(new FileServerPrivateEntity(dto));
                }
            }
            System.out.println(file.getPath()+", "+type+file.getName());
        }
        for(String folder : dirList){
            traversalFolder(path+File.separator+folder);
        }
    }
}
