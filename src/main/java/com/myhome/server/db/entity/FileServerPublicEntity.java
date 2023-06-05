package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPublicDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "FILE_PUBLIC_TB")
@NoArgsConstructor
public class FileServerPublicEntity {
    @Id
    @Column(name = "UUID_PK")
    private String uuid;
    @Column(name = "PATH_CHAR")
    private String path;
    @Column(name = "NAME_CHAR")
    private String name;
    @Column(name = "TYPE_CHAR")
    private String type;
    @Column(name = "SIZE_FLOAT")
    private float size;
    @Column(name = "LOCATION_CHAR")
    private String location;
    @Column(name = "STATE_INT")
    private  int state;

    @Builder
    public FileServerPublicEntity(String path, String name, String uuidName, String type, float size, String location, int state){
        this.path = path;
        this.name = name;
        this.uuid = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
    }

    @Builder
    public FileServerPublicEntity(FileServerPublicDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.uuid = dto.getUuidName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.location = dto.getLocation();
        this.state = dto.getState();
    }

    public void changePathAndLocation(String movePath, String location) {
        this.path = movePath;
        this.location = location;
    }
}
