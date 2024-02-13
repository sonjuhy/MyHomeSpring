package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerPublicDto {
    private String path;
    private String name;
    private String uuid;
    private String type;
    private float size;
    private String location;
    private int state;
    private int deleteStatus;

    @Builder
    public FileServerPublicDto(String path, String name, String uuidName, String type, float size, String location, int state, int delete){
        this.path = path;
        this.name = name;
        this.uuid = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
        this.deleteStatus =delete;
    }
}
