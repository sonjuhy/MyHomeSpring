package com.myhome.server.config;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import com.myhome.server.api.service.FileServerCommonService;
import com.myhome.server.api.service.FileServerPublicService;
import com.myhome.server.api.service.FileServerThumbNailService;
import com.myhome.server.component.batch.cloudPublic.CloudPublicFailedTasklet;
import com.myhome.server.component.batch.cloudPublic.CloudPublicTasklet;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import lombok.RequiredArgsConstructor;
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
public class BatchConfiguration {

    private final long dateTime = new Date().getTime();

    @Autowired
    private FileServerPublicService publicService;
    @Autowired
    private FileServerThumbNailService thumbNailService;
    @Autowired
    private FileServerCommonService commonService;
    @Autowired
    private FileServerThumbNailRepository thumbNailRepository;

    private final CloudPublicTasklet cloudPublicTasklet;
    private final CloudPublicFailedTasklet cloudPublicFailedTasklet;


    @Bean
    public Job publicCloudCheckJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){

        return new JobBuilder("CloudCheckJob-"+dateTime, jobRepository)
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
        return new FlowBuilder<SimpleFlow>("PublicCloudSplitFlow-"+dateTime)
                .split(publicCloudParallelTaskExecutor())
                .add(
                        publicCloudFlow("PublicCloudFlow-One", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Two", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Three", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Four", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Five", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Six", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Seven", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Eight", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Nine", jobRepository, platformTransactionManager),
                        publicCloudFlow("PublicCloudFlow-Ten", jobRepository, platformTransactionManager)
                )
                .build();
    }

    @Bean
    public TaskExecutor publicCloudParallelTaskExecutor(){
        return new SimpleAsyncTaskExecutor("CloudPublicParallelTask-"+dateTime);
    }

    @Bean
    public Flow publicCloudFlow(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new FlowBuilder<SimpleFlow>(name)
                .start(publicCloudParallelStep(name, jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public Step publicCloudParallelStep(String name, JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder(name, jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<FileInfoDto> fileList = (List<FileInfoDto>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(name);
                    if(fileList != null && !fileList.isEmpty()){
                        String uploadPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString("uploadPath");
                        String type = "public";
                        List<FileServerThumbNailEntity> entityList = new ArrayList<>();
                        for(FileInfoDto file : fileList){
                            String uuid = UUID.nameUUIDFromBytes(commonService.changeSeparatorToUnderBar(file.getPath()).getBytes(StandardCharsets.UTF_8)).toString();
                            String fileLocation = commonService.changeSeparatorToUnderBar(uploadPath+File.separator+uuid+".png");
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
                }, platformTransactionManager)
                .build();
    }
}
