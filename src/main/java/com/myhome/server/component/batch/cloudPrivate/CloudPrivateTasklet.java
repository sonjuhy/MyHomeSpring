package com.myhome.server.component.batch.cloudPrivate;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.enums.BatchEnum;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.UserService;
import com.myhome.server.db.entity.FileServerVideoEntity;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerVideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CloudPrivateTasklet implements Tasklet {

    @Autowired
    private FileServerPrivateService privateService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileDefaultPathRepository defaultPathRepository;
    @Autowired
    private FileServerVideoRepository videoRepository;
    @Autowired
    private UserService userService;

    @Transactional
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        privateService.deleteThumbNail();
        videoRepository.deleteAll();

        String diskPath = commonService.changeUnderBarToSeparator(defaultPathRepository.findByPathName("store").getPrivateDefaultPath());
        File defaultPath = new File(diskPath);
        File[] files = defaultPath.listFiles();
        log.info("CloudPrivateTasklet execute diskPath : "+diskPath);

        if(files != null) {
            log.info("CloudPrivateTasklet execute files length : " + files.length);
            List<File> fileList = new ArrayList<>();
            List<UserEntity> userList = userService.findAll();

            for (File file : files) {
                String fileName = file.getName();
                for(UserEntity entity : userList){
                    if(entity.getId().equals(fileName)){
                        String owner = entity.getId();
                        List<File> tmpFileList = privateService.filesWalkWithReturnMediaFileList(diskPath+File.separator+owner, owner);
                        log.info("CloudPrivateTasklet execute tmpFileList size : " + tmpFileList.size());
                        if(!tmpFileList.isEmpty() && tmpFileList.get(0) != null) fileList.addAll(tmpFileList);
                        break;
                    }
                }
            }
            if(fileList.isEmpty()){
                contribution.setExitStatus(ExitStatus.STOPPED);
                return RepeatStatus.FINISHED;
            }

            List<FileInfoDto> dtoList = new ArrayList<>();
            for(File file : fileList){
                FileInfoDto dto = new FileInfoDto();
                String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
                dto.setPath(file.getPath());
                dto.setUuid(uuid);
                dto.setName(file.getName());
                dtoList.add(dto);
            }

            int divNum = 3;
            int partitionSize = (int) Math.ceil((double) dtoList.size() / divNum);
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putInt(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getTarget(), partitionSize);

            List<FileServerVideoEntity> videoEntityList = dtoList.stream().map(FileServerVideoEntity::new).collect(Collectors.toList());
            videoRepository.saveAll(videoEntityList);
        }
        return RepeatStatus.FINISHED;
    }
}
