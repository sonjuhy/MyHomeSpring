package com.myhome.server.config.batch;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.api.enums.BatchEnum;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.FileServerThumbNailService;
import com.myhome.server.component.batch.cloudPrivate.CloudPrivateFailedTasklet;
import com.myhome.server.component.batch.cloudPrivate.CloudPrivateTasklet;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.entity.FileServerVideoEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import com.myhome.server.db.repository.FileServerVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BatchPrivateCloudConfiguration {
    private final long dateTime = new Date().getTime();
    @Autowired
    private FileServerPrivateService privateService;
    @Autowired
    private FileServerThumbNailService thumbNailService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileServerVideoRepository videoRepository;
    @Autowired
    private FileDefaultPathRepository defaultPathRepository;
    @Autowired
    private FileServerThumbNailRepository thumbNailRepository;
    
    private final CloudPrivateTasklet cloudPrivateTasklet;
    private final CloudPrivateFailedTasklet cloudPrivateFailedTasklet;
    
    @Bean
    public Job privateCloudCheckJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new JobBuilder("PrivateCloudCheckJob-"+dateTime, jobRepository)
                .start(privateCloudStep(jobRepository, platformTransactionManager))
                    .on(ExitStatus.STOPPED.getExitCode())
                    .end()
                    .on(ExitStatus.FAILED.getExitCode())
                    .to(privateCloudFailedStep(jobRepository, platformTransactionManager))
                    .on("*")
                    .end()
                .from(privateCloudStep(jobRepository, platformTransactionManager))
                    .on("*")
                    .to(privateCloudSplitFlow(jobRepository, platformTransactionManager))
                    .on("*")
                    .end()
                .end()
                .build();
    }

    @Bean
    public Step privateCloudStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("privateCloudCheckStep-"+dateTime, jobRepository)
                .tasklet(cloudPrivateTasklet, platformTransactionManager)
                .transactionManager(platformTransactionManager)
                .build();
    }

    @Bean
    public Step privateCloudFailedStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("privateCloudFailedStep-"+dateTime, jobRepository)
                .tasklet(cloudPrivateFailedTasklet, platformTransactionManager)
                .build();

    }

    @Bean
    public Flow privateCloudSplitFlow(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
        String uploadPath = commonService.changeUnderBarToSeparator(entity.getPublicDefaultPath());

        return new FlowBuilder<SimpleFlow>("privateCloudSplitFlow-" + dateTime)
                .split(new SimpleAsyncTaskExecutor())
                .add(
                        privateCloudFlow1(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getPublicParallelFlowName(1), jobRepository, platformTransactionManager, uploadPath),
                        privateCloudFlow2(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getPublicParallelFlowName(2), jobRepository, platformTransactionManager, uploadPath),
                        privateCloudFlow3(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getPublicParallelFlowName(3), jobRepository, platformTransactionManager, uploadPath)
                )
                .build();
    }

    @Bean
    public Flow privateCloudFlow1(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new FlowBuilder<SimpleFlow>(name)
                .start(privateCloudParallelStep1(name, jobRepository, platformTransactionManager, uploadPath))
                .build();
    }

    @Bean
    public Step privateCloudParallelStep1(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getTarget());
                    Pageable pageable = PageRequest.of(0, partitionSize);
                    List<FileServerVideoEntity> videoEntityList = videoRepository.findAllBy(pageable);

                    List<FileInfoDto> fileList = new ArrayList<>();
                    for(FileServerVideoEntity entity : videoEntityList){
                        FileInfoDto tmpDto = new FileInfoDto();
                        tmpDto.setName(entity.getName());
                        tmpDto.setUuid(entity.getUuid());
                        tmpDto.setPath(entity.getPath());
                        fileList.add(tmpDto);
                    }

                    if(!fileList.isEmpty()){
                        thumbNailSequence(fileList, uploadPath);
                    }
                    else{
                        contribution.setExitStatus(ExitStatus.FAILED);
                    }
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Flow privateCloudFlow2(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new FlowBuilder<SimpleFlow>(name)
                .start(privateCloudParallelStep2(name, jobRepository, platformTransactionManager, uploadPath))
                .build();
    }

    @Bean
    public Step privateCloudParallelStep2(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getTarget());
                    Pageable pageable = PageRequest.of(1, partitionSize);
                    List<FileServerVideoEntity> videoEntityList = videoRepository.findAllBy(pageable);

                    List<FileInfoDto> fileList = new ArrayList<>();
                    for(FileServerVideoEntity entity : videoEntityList){
                        FileInfoDto tmpDto = new FileInfoDto();
                        tmpDto.setName(entity.getName());
                        tmpDto.setUuid(entity.getUuid());
                        tmpDto.setPath(entity.getPath());
                        fileList.add(tmpDto);
                    }

                    if(!fileList.isEmpty()){
                        thumbNailSequence(fileList, uploadPath);
                    }
                    else{
                        contribution.setExitStatus(ExitStatus.FAILED);
                    }
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public Flow privateCloudFlow3(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new FlowBuilder<SimpleFlow>(name)
                .start(privateCloudParallelStep3(name, jobRepository, platformTransactionManager, uploadPath))
                .build();
    }

    @Bean
    public Step privateCloudParallelStep3(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, String uploadPath){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PRIVATE_CHUNK_PARTITION_NAME.getTarget());
                    Pageable pageable = PageRequest.of(2, partitionSize);
                    List<FileServerVideoEntity> videoEntityList = videoRepository.findAllBy(pageable);

                    List<FileInfoDto> fileList = new ArrayList<>();
                    for(FileServerVideoEntity entity : videoEntityList){
                        FileInfoDto tmpDto = new FileInfoDto();
                        tmpDto.setName(entity.getName());
                        tmpDto.setUuid(entity.getUuid());
                        tmpDto.setPath(entity.getPath());
                        fileList.add(tmpDto);
                    }

                    if(!fileList.isEmpty()){
                        thumbNailSequence(fileList, uploadPath);
                    }
                    else{
                        contribution.setExitStatus(ExitStatus.FAILED);
                    }
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }



    private void thumbNailSequence(List<FileInfoDto> fileList, String uploadPath){
        String type = "private";
        List<FileServerThumbNailEntity> entityList = new ArrayList<>();
        for(FileInfoDto file : fileList){
            log.info("thumbNailSequence file str : " + file.toString());
            String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
            String fileLocation = commonService.changeSeparatorToUnderBar(uploadPath+ File.separator+uuid+".jpg");
            FileServerThumbNailDto thumbNailDto = new FileServerThumbNailDto(0, file.getUuid(), fileLocation, file.getName(), type);
            File tmpFile = new File(file.getPath());
            if(thumbNailService.makeThumbNail(tmpFile, uuid, type)){
                entityList.add(new FileServerThumbNailEntity(thumbNailDto));
            }
        }
        thumbNailRepository.saveAll(entityList);
    }
}
