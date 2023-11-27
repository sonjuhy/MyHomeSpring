package com.myhome.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi apkUpdateApi(){
        return GroupedOpenApi.builder()
                .group("apkUpdate-control")
                .pathsToMatch("/apkUpdate/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi(){
        return GroupedOpenApi.builder()
                .group("auth-control")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi cloudApi(){ // FileServer
        return GroupedOpenApi.builder()
                .group("cloud-control")
                .pathsToMatch("/file/**")
                .build();
    }

    @Bean
    public GroupedOpenApi lightApi(){
        return GroupedOpenApi.builder()
                .group("light-control")
                .pathsToMatch("/light/**")
                .build();
    }

    @Bean
    public GroupedOpenApi noticeApi(){
        return GroupedOpenApi.builder()
                .group("notice-control")
                .pathsToMatch("/notice/**")
                .build();
    }

    @Bean
    public GroupedOpenApi weatherApi(){
        return GroupedOpenApi.builder()
                .group("weather-control")
                .pathsToMatch("/weather/**")
                .build();
    }

    @Bean
    public GroupedOpenApi WOLApi(){
        return GroupedOpenApi.builder()
                .group("wol-control")
                .pathsToMatch("/wol/**")
                .build();
    }

    @Bean
    public OpenAPI myHomeOpenAPI(){
        return new OpenAPI()
                .info(new Info().title("MyHome Spring API")
                        .description("MyHome Spring Boot Back-End System API")
                        .version("v0.1"));

    }
}
