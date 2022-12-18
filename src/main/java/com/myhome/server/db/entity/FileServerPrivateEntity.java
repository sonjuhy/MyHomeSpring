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
@Table(name = "fileserver_private")
@NoArgsConstructor
public class FileServerPrivateEntity {
    @Id
    @Column(name = "path")
    private String path;
    @Column(name = "name")
    private String name;
    @Column(name = "uuid_name")
    private String uuidName;
    @Column(name = "type")
    private String type;
    @Column(name = "size")
    private float size;
    @Column(name = "owner")
    private String owner;
    @Column(name = "location")
    private String location;

    @Builder
    public FileServerPrivateEntity(String path, String name, String uuidName, String type, float size, String owner, String location){
        this.path = path;
        this.name = name;
        this.uuidName = uuidName;
        this.type = type;
        this.size = size;
        this.owner = owner;
        this.location = location;
    }

    @Builder
    public FileServerPrivateEntity(FileServerPrivateDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.owner = dto.getOwner();
        this.location = dto.getLocation();
    }
}
