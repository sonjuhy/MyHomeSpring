package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPrivateDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "FILE_PRIVATE_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerPrivateEntity {
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
    @Column(name = "OWNER_CHAR")
    private String owner;
    @Column(name = "LOCATION_CHAR")
    private String location;
    @Column(name = "STATE_INT")
    private int state;
    @Column(name = "DELETE_STATUS_INT")
    private int delete;

    @Builder
    public FileServerPrivateEntity(String path, String name, String uuidName, String type, float size, String owner, String location, int state, int delete){
        this.path = path;
        this.name = name;
        this.uuid = uuidName;
        this.type = type;
        this.size = size;
        this.owner = owner;
        this.location = location;
        this.state = state;
        this.delete = delete;
    }

    @Builder
    public FileServerPrivateEntity(FileServerPrivateDto dto){
        this.path = dto.getPath();
        this.uuid = dto.getUuidName();
        this.name = dto.getName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.owner = dto.getOwner();
        this.location = dto.getLocation();
        this.state = dto.getState();
        this.delete = dto.getDelete();
    }

    public void changePathAndLocation(String movePath, String location) {
        this.path = movePath;
        this.location = location;
    }
}
