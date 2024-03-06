package com.myhome.server.component.batch.cloudPublic;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.db.entity.FileServerVideoEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerVideoRepository;
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

@Component
public class CloudPublicTasklet implements Tasklet {

    @Autowired
    private FileServerPublicService publicService;
    @Autowired
    private FileServerCommonService commonService;

    @Autowired
    FileDefaultPathRepository defaultPathRepository;
    @Autowired
    FileServerVideoRepository videoRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        publicService.deleteThumbNail();
        videoRepository.deleteAll();

        List<File> fileList = publicService.filesWalkWithReturnMediaFileList();
        List<FileInfoDto> dtoList = new ArrayList<>();
        List<FileServerVideoEntity> videoEntityList = new ArrayList<>();

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

        videoEntityList = dtoList.stream().map(FileServerVideoEntity::new).collect(Collectors.toList());
        videoRepository.saveAll(videoEntityList);
//        int divNum = 3;
//        int partitionSize = (int) Math.ceil((double) dtoList.size() / divNum);
//        List<List<FileInfoDto>> groups = IntStream.range(0, divNum)
//                .mapToObj(i -> dtoList.subList(i * partitionSize, Math.min((i + 1) * partitionSize, dtoList.size())))
//                .toList();
//
//        divNum = groups.size();
//        for(int i=0;i<divNum;i++){
//            List<FileInfoDto> group = groups.get(i);
//            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("PublicCloudFlow-"+(i+1), new ArrayList<>(group));
//        }
//
//        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
//        String uploadPath = commonService.changeUnderBarToSeparator(entity.getPublicDefaultPath());
//        chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("uploadPath", uploadPath);
        return RepeatStatus.FINISHED;
    }

}
