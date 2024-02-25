package com.myhome.server.api.dto;

import com.myhome.server.db.entity.FileServerPublicTrashEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerPublicTrashDto {
    private String path;
    private String originPath;
    private String name;
    private String uuidName;
    private String type;
    private float size;
    private String location;
    private int state;

    @Builder
    public FileServerPublicTrashDto(String path, String originPath, String name, String uuidName, String type, float size, String location, int state) {
        this.path = path;
        this.originPath = originPath;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
    }

    public FileServerPublicTrashDto(FileServerPublicTrashEntity entity){
        this.path = entity.getPath();
        this.originPath = entity.getOriginPath();
        this.name = entity.getName();
        this.uuidName = entity.getUuid();
        this.type = entity.getType();
        this.size = entity.getSize();
        this.location = entity.getLocation();
        this.state = entity.getState();
    }
}
