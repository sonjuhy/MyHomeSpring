package com.myhome.server.component.batch.cloudPublic;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.db.entity.FileServerVideoEntity;
import com.myhome.server.db.repository.FileServerVideoRepository;
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

@Component
public class CloudPublicTasklet implements Tasklet {

    @Autowired
    private FileServerPublicService publicService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    FileServerVideoRepository videoRepository;

    @Transactional
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        publicService.deleteThumbNail();
        videoRepository.deleteAll();

        List<File> fileList = publicService.filesWalkWithReturnMediaFileList();
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
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putInt("PublicCloudPartition", partitionSize);

        List<FileServerVideoEntity> videoEntityList = dtoList.stream().map(FileServerVideoEntity::new).collect(Collectors.toList());
        videoRepository.saveAll(videoEntityList);
        return RepeatStatus.FINISHED;
    }


}
