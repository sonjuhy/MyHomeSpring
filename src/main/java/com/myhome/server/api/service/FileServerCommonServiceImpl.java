package com.myhome.server.api.service;

import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.regex.Matcher;

@Service
public class FileServerCommonServiceImpl implements FileServerCommonService{

    @Autowired
    private FileDefaultPathRepository defaultPathRepository;
    @Override
    public int[] getStorageUsage(String mode) {
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("top");
        String topPath = entity.getPublicDefaultPath();
        File file = new File(changeUnderBarToSeparator(topPath));
        int[] usage = new int[2];
        switch(mode){
            case "MB":
                usage[0] = toMB(file.getTotalSpace());
                usage[1] = toMB(file.getFreeSpace());
                break;
            case "GB":
                usage[0] = toGB(file.getTotalSpace());
                usage[1] = toGB(file.getFreeSpace());
                break;
            case "percent":
                int total = toGB(file.getTotalSpace());
                int free = toGB(file.getFreeSpace());
                usage[0] = 100;
                usage[1] = free/total*100;
                break;
        }
        return usage;
    }
    public int toMB(long size){
        return (int)(size/(1024*1024));
    }
    public int toGB(long size){
        return (int)(size/(1024*1024*1024));
    }
    @Override
    public String changeUnderBarToSeparator(String path){
        return path.replaceAll("__", Matcher.quoteReplacement(File.separator));
    }
    @Override
    public String changeSeparatorToUnderBar(String path){
        return path.replaceAll(Matcher.quoteReplacement(File.separator), "__");
    }
}
