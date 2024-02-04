package com.myhome.server.component.batch.cloudPublic;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CloudPublicTasklet implements Tasklet {

    @Autowired
    private FileServerPublicService publicService;

    @Autowired
    private FileServerCommonService commonService;

    @Autowired
    FileDefaultPathRepository defaultPathRepository;
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<File> fileList = publicService.filesWalkWithReturnMediaFileList();
        List<FileInfoDto> dtoList = new ArrayList<>();

        fileList = new ArrayList<>(fileList);

        for(File file : fileList){
            FileInfoDto dto = new FileInfoDto();
            String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
            dto.setPath(file.getPath());
            dto.setUuid(uuid);
            dtoList.add(dto);
        }

        int divNum = 10;
        int partitionSize = (int) Math.ceil((double) dtoList.size() / divNum);
        List<List<FileInfoDto>> groups = IntStream.range(0, divNum)
                .mapToObj(i -> dtoList.subList(i * partitionSize, Math.min((i + 1) * partitionSize, dtoList.size())))
                .toList();
        String[] names = new String[divNum];
        names[0] = "PublicCloudFlow-One";
        names[1] = "PublicCloudFlow-Two";
        names[2] = "PublicCloudFlow-Three";
        names[3] = "PublicCloudFlow-Four";
        names[4] = "PublicCloudFlow-Five";
        names[5] = "PublicCloudFlow-Six";
        names[6] = "PublicCloudFlow-Seven";
        names[7] = "PublicCloudFlow-Eight";
        names[8] = "PublicCloudFlow-Nine";
        names[9] = "PublicCloudFlow-Ten";

        divNum = groups.size();
        for(int i=0;i<divNum;i++){
            List<FileInfoDto> group = groups.get(i);
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(names[i], group);
        }
//        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("fileList", fileList);

        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
        String uploadPath = entity.getPublicDefaultPath().replaceAll("__", File.separator);
        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("uploadPath", uploadPath);
        return RepeatStatus.FINISHED;
    }

}
