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
    public int[] getStorageUsage() {
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("top");
        String topPath = entity.getPublicDefaultPath();
        File file = new File(changeUnderBarToSeparator(topPath));
        int[] usage = new int[2];
        usage[0] = toMB(file.getTotalSpace());
        usage[1] = toMB(file.getFreeSpace());
        return usage;
    }
    public int toMB(long size){
        return (int)(size/(1024*1024));
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
