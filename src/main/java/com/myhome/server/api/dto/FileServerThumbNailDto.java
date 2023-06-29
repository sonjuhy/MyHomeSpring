package com.myhome.server.api.dto;

import lombok.*;

import javax.persistence.Column;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerThumbNailDto {
    private String uuid;
    private String path;
    private String originName;

//    @Builder
//    public FileServerThumbNailDto(){}

    @Builder
    public FileServerThumbNailDto(String uuid, String path, String originName){
        this.uuid = uuid;
        this.path = path;
        this.originName = originName;
    }
}
