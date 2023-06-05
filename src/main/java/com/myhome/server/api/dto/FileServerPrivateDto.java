package com.myhome.server.api.dto;

import lombok.*;

import javax.persistence.Column;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerPrivateDto {
    private String path;
    private String name;
    private String uuidName;
    private String type;
    private float size;
    private String owner;
    private String location;
    private int state;

    @Builder
    public FileServerPrivateDto(String path, String name, String uuidName, String type, float size, String owner, String location, int state){
        this.path = path;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.owner = owner;
        this.location = location;
        this.state = state;
    }
}
