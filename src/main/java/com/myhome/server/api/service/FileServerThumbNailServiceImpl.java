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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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

    @Async
    @Override
    public CompletableFuture<List<FileServerThumbNailEntity>> setThumbNail(List<File> files, String type) {
        List<FileServerThumbNailEntity> entityList = new ArrayList<>();
        for(File file : files){
            String uuid = UUID.nameUUIDFromBytes(changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
            String fileLocation = changeSeparatorToUnderBar(uploadPath+File.separator+uuid+".png");
            FileServerThumbNailDto thumbNailDto = new FileServerThumbNailDto(0, uuid, fileLocation, file.getName(), type);

            if(makeThumbNail(file, uuid, type)){
                entityList.add(new FileServerThumbNailEntity(thumbNailDto));
            }
        }
        return CompletableFuture.completedFuture(entityList);
    }

    @Transactional
    @Override
    public boolean makeThumbNail(File file, String uuid, String type) {
        File thumbnail = new File(uploadPath, uuid+".jpg");
        try{
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

            // 첫 프레임의 데이터
            frameGrab.seekToSecondPrecise(1);

            Picture picture = frameGrab.getNativeFrame();

            // 썸네일 파일에 복사
            BufferedImage bi = AWTUtil.toBufferedImage(picture);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if(!writers.hasNext()) {
                ImageIO.write(bi, "jpg", thumbnail);
            }
            else{
                OutputStream os = new FileOutputStream(thumbnail);

                float quality = 0.2f;

                ImageWriter imageWriter = writers.next();
                ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(os);
                imageWriter.setOutput(imageOutputStream);

                ImageWriteParam param = imageWriter.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                imageWriter.write(null, new IIOImage(bi, null, null), param);
                os.close();
                imageOutputStream.close();
                imageWriter.dispose();
            }

        } catch (Exception e) {
            if(thumbnail.exists()) {
                thumbnail.delete();
            }
            System.out.println("makeThumbNail error : "+e.getMessage());
//            logComponent.sendErrorLog("Cloud-Check", "makeThumbNail Error : ", e, TOPIC_CLOUD_LOG);
            return false;
        }
        return true;
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
