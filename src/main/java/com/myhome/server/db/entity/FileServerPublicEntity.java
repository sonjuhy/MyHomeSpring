package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPublicDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "FILE_PUBLIC_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerPublicEntity {
    @Id
    @Column(name = "ID_PK")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "UUID_CHAR")
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
    @Column(name = "DELETE_STATUS_INT")
    private int delete;

    @Builder
    public FileServerPublicEntity(int id, String path, String name, String uuidName, String type, float size, String location, int state, int delete){
        this.id = id;
        this.path = path;
        this.name = name;
        this.uuid = uuidName;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
        this.delete = delete;
    }

    public FileServerPublicEntity(FileServerPublicDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.uuid = dto.getUuid();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.location = dto.getLocation();
        this.state = dto.getState();
        this.delete = dto.getDeleteStatus();
    }

    public void changePathAndLocation(String movePath, String location) {
        this.path = movePath;
        this.location = location;
    }
}
