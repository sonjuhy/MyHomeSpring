package com.myhome.server.config.batch;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.api.enums.BatchEnum;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.api.service.FileServerThumbNailService;
import com.myhome.server.component.batch.cloudPublic.CloudPublicFailedTasklet;
import com.myhome.server.component.batch.cloudPublic.CloudPublicTasklet;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.entity.FileServerVideoEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import com.myhome.server.db.repository.FileServerVideoRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Configuration
public class BatchPublicCloudConfiguration {

    private final long dateTime = new Date().getTime();
    private String uploadPath;

    @Autowired
    private FileServerPublicService publicService;
    @Autowired
    private FileServerThumbNailService thumbNailService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileServerThumbNailRepository thumbNailRepository;
    @Autowired
    private FileServerVideoRepository videoRepository;
    @Autowired
    private FileDefaultPathRepository defaultPathRepository;

    private final CloudPublicTasklet cloudPublicTasklet;
    private final CloudPublicFailedTasklet cloudPublicFailedTasklet;


    @Bean
    public Job publicCloudCheckJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){

        return new JobBuilder("PublicCloudCheckJob-"+dateTime, jobRepository)
                .start(publicCloudStep(jobRepository, platformTransactionManager))
                    .on(ExitStatus.FAILED.getExitCode())
                    .to(publicCloudFailedStep(jobRepository, platformTransactionManager))
                    .on("*")
                    .end()
                .from(publicCloudStep(jobRepository, platformTransactionManager))
                    .on("*")
                    .to(publicCloudSplitFlow(jobRepository, platformTransactionManager))
                    .on("*")
                    .end()
                .end()
                .build();
    }

    @Bean
    public Step publicCloudStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("PublicCloudCheckStep-"+dateTime, jobRepository)
                .tasklet(cloudPublicTasklet, platformTransactionManager)
                .transactionManager(platformTransactionManager)
                .build();
    }

    @Bean
    public Step publicCloudFailedStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("PublicCloudFailedStep-"+dateTime, jobRepository)
                .tasklet(cloudPublicFailedTasklet, platformTransactionManager)
                .build();

    }

    @Bean
    public Flow publicCloudSplitFlow(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        FileDefaultPathEntity entity = defaultPathRepository.findByPathName("thumbnail");
        uploadPath = commonService.changeUnderBarToSeparator(entity.getPublicDefaultPath());

        return new FlowBuilder<SimpleFlow>("PublicCloudSplitFlow-" + dateTime)
                .split(new SimpleAsyncTaskExecutor())
                .add(
                        publicCloudFlow1(BatchEnum.CLOUD_PRIVATE_PARALLEL_FLOW_NAME.getPublicParallelFlowName(1), jobRepository, platformTransactionManager),
                        publicCloudFlow2(BatchEnum.CLOUD_PRIVATE_PARALLEL_FLOW_NAME.getPublicParallelFlowName(2), jobRepository, platformTransactionManager),
                        publicCloudFlow3(BatchEnum.CLOUD_PRIVATE_PARALLEL_FLOW_NAME.getPublicParallelFlowName(3), jobRepository, platformTransactionManager)
                )
                .build();
    }

    @Bean
    public Flow publicCloudFlow1(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new FlowBuilder<SimpleFlow>(name)
                .start(publicCloudParallelStep1(name, jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step publicCloudParallelStep1(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PUBLIC_CHUNK_PARTITION_NAME.getTarget());
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
    public Flow publicCloudFlow2(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new FlowBuilder<SimpleFlow>(name)
                .start(publicCloudParallelStep2(name, jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step publicCloudParallelStep2(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PUBLIC_CHUNK_PARTITION_NAME.getTarget());
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
    public Flow publicCloudFlow3(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new FlowBuilder<SimpleFlow>(name)
                .start(publicCloudParallelStep3(name, jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step publicCloudParallelStep3(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder(name, jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int partitionSize = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getInt(BatchEnum.CLOUD_PUBLIC_CHUNK_PARTITION_NAME.getTarget());
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
        String type = "public";
        List<FileServerThumbNailEntity> entityList = new ArrayList<>();
        for(FileInfoDto file : fileList){
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
