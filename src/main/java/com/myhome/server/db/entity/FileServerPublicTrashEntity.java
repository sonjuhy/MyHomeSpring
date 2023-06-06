package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPublicDto;
import com.myhome.server.api.dto.FileServerPublicTrashDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "FILE_PUBLIC_TRASH_TB")
@NoArgsConstructor
public class FileServerPublicTrashEntity {
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
    public FileServerPublicTrashEntity(FileServerPublicTrashDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.uuid = dto.getUuidName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.location = dto.getLocation();
        this.state = dto.getState();
    }
}
