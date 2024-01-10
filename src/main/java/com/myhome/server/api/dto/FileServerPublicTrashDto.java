package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerPublicTrashDto {
    private String path;
    private String name;
    private String uuidName;
    private String type;
    private float size;
    private String location;
    private int state;

    @Builder
    public FileServerPublicTrashDto(String path, String name, String uuidName, String type, float size, String location, int state) {
        this.path = path;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
    }
}
