package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPublicTrashDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "FILE_PUBLIC_TRASH_TB")
@NoArgsConstructor
public class FileServerPublicTrashEntity {
    @Id
    @Column(name = "ID_PK")
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

    @Builder
    public FileServerPublicTrashEntity(int id, String uuid, String path, String name, String type, float size, String location, int state) {
        this.id = id;
        this.uuid = uuid;
        this.path = path;
        this.name = name;
        this.type = type;
        this.size = size;
        this.location = location;
        this.state = state;
    }
    public FileServerPublicTrashEntity(FileServerPublicTrashDto dto){
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
