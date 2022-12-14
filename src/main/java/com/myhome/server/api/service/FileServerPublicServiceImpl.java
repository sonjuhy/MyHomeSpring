package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileServerPublicEntity;
import com.myhome.server.db.repository.FileServerPublicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class FileServerPublicServiceImpl implements FileServerPublicService {

    private final String diskPath = "/home/disk1/home/public";
    private final String trashPath = "/home/disk1/home/public/휴지통";

    @Autowired
    FileServerPublicRepository repository;

    @Override
    public FileServerPublicEntity findByPath(String path) {
        FileServerPublicEntity entity = repository.findByPath(path);
        return entity;
    }

    @Override
    public List<FileServerPublicEntity> findByLocation(String location) {
        List<FileServerPublicEntity> list = repository.findByLocation(location);
        return list;
    }

    @Override
    public boolean existsByFileServerPublicEntity(FileServerPublicEntity entity) {
//        boolean result = repository.existsByFileServerPublicEntity(entity);
        return false;
    }

//    @Transactional
    @Override
    public long deleteByPath(String path) {
        FileServerPublicEntity entity = repository.findByPath(path);
        if(ObjectUtils.isEmpty(entity)){
            return -1;
        }
        String testInPath = "C:\\Users\\SonJunHyeok\\Desktop\\a.txt";
        String testOutPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\a.txt";
        try{
            File in = new File(testInPath);
            File out = new File(testOutPath);
            FileCopyUtils.copy(in, out);
            if(in.exists()){
                if(in.delete()){
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
        long result = repository.deleteByPath(path);
        return result;
    }

    @Override
    public int updateByFileServerPublicEntity(FileServerPublicEntity entity) {
//        int result = repository.updateByFileServerPublicEntity(entity);
        return 0;
    }

    @Override
    public boolean save(FileServerPublicEntity entity) {

        return !ObjectUtils.isEmpty(repository.save(entity));
    }
}
