package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPrivateDto;
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
@Table(name = "FILE_PRIVATE_TB")
@NoArgsConstructor
public class FileServerPrivateEntity {
    @Id
    @Column(name = "UUID_PK")
    private String uuidName;
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

    @Builder
    public FileServerPrivateEntity(String path, String name, String uuidName, String type, float size, String owner, String location, int state){
        this.path = path;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.owner = owner;
        this.location = location;
        this.state = state;
    }

    @Builder
    public FileServerPrivateEntity(FileServerPrivateDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.owner = dto.getOwner();
        this.location = dto.getLocation();
        this.state = dto.getState();
    }
}
