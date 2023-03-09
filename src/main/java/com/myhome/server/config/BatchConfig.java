package com.myhome.server.config;

import com.myhome.server.db.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

//    @Bean
//    public Job exampleJob() throws  Exception{
//        return jobBuilderFactory.get("exampleJob")
//                .start(exampleStep()).build();
//    }

//    @Bean
//    @JobScope
//    public Step exampleStep() throws Exception{
//        return stepBuilderFactory.get("exampleStep")
//                .<UserEntity, UserEntity>chunk(10)
//                .reader(reader(null))
//                .processor(processor(null))
//                .writer(writer(null))
//                .build();
//    }
//
//    @Bean
//    @StepScope
//    public JpaPagingItemReader<UserEntity> reader(@Value("#{jobParameters[requestDate]}") String requestData) throws Exception{
//
//        return null;
////        return new JpaPagingItemReaderBuilder<UserEntity>()
////                .pageSize(10)
////                .parameterValues(parameterValues)
////                .queryString()
//    }
}
