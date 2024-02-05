package com.myhome.server.component.batch.cloudPrivate;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.UserService;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
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
import java.util.stream.IntStream;

@Component
public class CloudPrivateTasklet implements Tasklet {

    @Autowired
    private FileServerPrivateService privateService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileDefaultPathRepository defaultPathRepository;
    @Autowired
    private UserService userService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String diskPath = defaultPathRepository.findByPathName("store").getPrivateDefaultPath();
        File defaultPath = new File(diskPath);
        File[] files = defaultPath.listFiles();

        if(files != null) {
            List<File> fileList = new ArrayList<>();
            List<UserEntity> userList = userService.findAll();
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                String fileName = file.getName();
                sb.append(fileName).append("\n");
                for(UserEntity entity : userList){
                    if(entity.getId().equals(fileName)){
                        String owner = entity.getId();
                        List<File> tmpFileList = privateService.filesWalkWithReturnMediaFileList(diskPath+File.separator+owner, owner);
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
            List<List<FileInfoDto>> groups = IntStream.range(0, divNum)
                    .mapToObj(i -> dtoList.subList(i * partitionSize, Math.min((i + 1) * partitionSize, dtoList.size())))
                    .toList();

            divNum = groups.size();
            for(int i=0;i<divNum;i++){
                List<FileInfoDto> group = groups.get(i);
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("PrivateCloudFlow-"+(i+1), new ArrayList<>(group));
            }
            FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
            String uploadPath = commonService.changeUnderBarToSeparator(entity.getPublicDefaultPath());
            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("uploadPath", uploadPath);
        }
        else{
            contribution.setExitStatus(ExitStatus.STOPPED);
        }
        return RepeatStatus.FINISHED;
    }
}
