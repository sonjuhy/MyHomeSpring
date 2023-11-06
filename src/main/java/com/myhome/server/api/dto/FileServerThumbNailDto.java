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
    private String type;

    @Builder

    public FileServerThumbNailDto(String uuid, String path, String originName, String type) {
        this.uuid = uuid;
        this.path = path;
        this.originName = originName;
        this.type = type;
    }
}
