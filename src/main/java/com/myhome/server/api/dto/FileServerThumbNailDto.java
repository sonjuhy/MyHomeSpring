package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerThumbNailDto {
    private long id;
    private String uuid;
    private String path;
    private String originName;
    private String type;

    @Builder
    public FileServerThumbNailDto(long id, String uuid, String path, String originName, String type) {
        this.id = id;
        this.uuid = uuid;
        this.path = path;
        this.originName = originName;
        this.type = type;
    }
}
