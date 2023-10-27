package com.myhome.server.api.service;

import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class FileServerThumbNailServiceImpl implements FileServerThumbNailService {

    private static final String uploadPath = "C:\\Users\\SonJunHyeok\\Desktop\\test\\thumbnail";

    @Autowired
    FileServerThumbNailRepository repository;

    @Override
    public void deleteByUUID(String uuid) {
        repository.deleteByUuid(uuid);
    }

    @Override
    public FileServerThumbNailEntity findByUUID(String uuid) {
        return repository.findByUuid(uuid);
    }

    @Override
    public void makeThumbNail(File file, String uuid) {
        System.out.println("makeThumbNail : " + file.getName());
        File thumbnail = new File(uploadPath, uuid+".png");
        try{
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(file));

            // 첫 프레임의 데이터
            frameGrab.seekToSecondPrecise(0);

            Picture picture = frameGrab.getNativeFrame();

            // 썸네일 파일에 복사
            BufferedImage bi = AWTUtil.toBufferedImage(picture);
            ImageIO.write(bi, "png", thumbnail);

            String fileLocation = uploadPath+File.separator+uuid+".png";
            FileServerThumbNailDto thumbNailDto = new FileServerThumbNailDto(uuid, fileLocation, file.getName());
            repository.save(new FileServerThumbNailEntity(thumbNailDto));

        } catch (JCodecException | IOException e) {
            e.printStackTrace();
        }
    }
}
