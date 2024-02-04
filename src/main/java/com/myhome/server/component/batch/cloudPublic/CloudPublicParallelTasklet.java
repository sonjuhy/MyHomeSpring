package com.myhome.server.component.batch.cloudPublic;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerThumbNailService;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CloudPublicParallelTasklet implements Tasklet {

    @Autowired
    private FileServerThumbNailService thumbNailService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileServerThumbNailRepository thumbNailRepository;
    private String name = "";
    public void setName(String name){
        this.name = name;
    }
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<FileInfoDto> fileList = (List<FileInfoDto>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(name);
        if(fileList != null && !fileList.isEmpty()){
            String uploadPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString("uploadPath");
            String type = "public";
            List<FileServerThumbNailEntity> entityList = new ArrayList<>();
            for(FileInfoDto file : fileList){
                String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
                String fileLocation = commonService.changeSeparatorToUnderBar(uploadPath+ File.separator+uuid+".png");
                FileServerThumbNailDto thumbNailDto = new FileServerThumbNailDto(0, file.getUuid(), fileLocation, file.getName(), type);
                File tmpFile = new File(file.getPath());
                if(thumbNailService.makeThumbNail(tmpFile, uuid, type)){
                    entityList.add(new FileServerThumbNailEntity(thumbNailDto));
                }
            }
            thumbNailRepository.saveAll(entityList);
        }
        else{
            contribution.setExitStatus(ExitStatus.FAILED);
        }
        return RepeatStatus.FINISHED;
    }
}
