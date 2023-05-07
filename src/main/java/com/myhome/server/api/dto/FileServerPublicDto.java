package com.myhome.server.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileServerPublicDto {
    private String path;
    private String name;
    private String uuidName;
    private String type;
    private float size;
    private String location;
    private int state;

    @Builder
    public FileServerPublicDto(String path, String name, String uuidName, String type, float size, String location, int state){
        this.path = path;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
    }
}
