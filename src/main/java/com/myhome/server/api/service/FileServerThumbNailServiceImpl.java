package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

@Service
public class FileServerThumbNailServiceImpl implements FileServerThumbNailService {

    private final String uploadPath;

    private final static String TOPIC_CLOUD_LOG = "cloud-log-topic";

    @Autowired
    LogComponent logComponent;

    @Autowired
    FileServerThumbNailRepository repository;

    @Autowired
    public FileServerThumbNailServiceImpl(FileDefaultPathRepository defaultPathRepository){
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
        uploadPath = changeUnderBarToSeparator(entity.getPublicDefaultPath());
    }

    @Override
    public void deleteByUUID(String uuid) {
        repository.deleteByUuid(uuid);
    }

    @Override
    public FileServerThumbNailEntity findByUUID(String uuid) {
        return repository.findByUuid(uuid);
    }

    @Transactional
    @Override
    public void makeThumbNail(File file, String uuid, String type) {
        File thumbnail = new File(uploadPath, uuid+".png");
        try{
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

            // 첫 프레임의 데이터
            frameGrab.seekToSecondPrecise(0);

            Picture picture = frameGrab.getNativeFrame();

            // 썸네일 파일에 복사
            BufferedImage bi = AWTUtil.toBufferedImage(picture);
            ImageIO.write(bi, "png", thumbnail);

        } catch (JCodecException | IOException e) {
            logComponent.sendErrorLog("Cloud-Check", "makeThumbNail Error : ", e, TOPIC_CLOUD_LOG);
        }
        if(thumbnail.exists()) {
            String fileLocation = changeSeparatorToUnderBar(uploadPath+File.separator+uuid+".png");
            FileServerThumbNailDto thumbNailDto = new FileServerThumbNailDto(0, uuid, fileLocation, file.getName(), type);
            repository.save(new FileServerThumbNailEntity(thumbNailDto));
        }
    }

    @Override
    public boolean checkThumbNailExist(String uuid) {
        return repository.existsByUuid(uuid);
    }

    private String changeUnderBarToSeparator(String path){
        return path.replaceAll("__", Matcher.quoteReplacement(File.separator));
    }
    private String changeSeparatorToUnderBar(String path){
        return path.replaceAll(Matcher.quoteReplacement(File.separator), "__");
    }
}
