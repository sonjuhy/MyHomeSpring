package com.myhome.server.config.batch;

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
public class BatchPublicCloudConfiguration {

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
        return new FlowBuilder<SimpleFlow>("PublicCloudSplitFlow-" + dateTime)
                .split(new SimpleAsyncTaskExecutor())
                .add(
                        publicCloudFlow1("PublicCloudFlow-1", jobRepository, platformTransactionManager),
                        publicCloudFlow2("PublicCloudFlow-2", jobRepository, platformTransactionManager),
                        publicCloudFlow3("PublicCloudFlow-3", jobRepository, platformTransactionManager)
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
                    List<FileInfoDto> fileList = (List<FileInfoDto>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(name);
                    if(fileList != null && !fileList.isEmpty()){
                        String uploadPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString("uploadPath");
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
                    List<FileInfoDto> fileList = (List<FileInfoDto>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(name);
                    if(fileList != null && !fileList.isEmpty()){
                        String uploadPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString("uploadPath");
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
                    List<FileInfoDto> fileList = (List<FileInfoDto>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(name);
                    if(fileList != null && !fileList.isEmpty()){
                        String uploadPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString("uploadPath");
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
