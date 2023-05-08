package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.FileServerPublicDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "fileserver_private")
@NoArgsConstructor
public class FileServerPrivateEntity {
    @Id
    @Column(name = "uuid_name")
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private String uuidName;
    @Column(name = "path")
    private String path;
    @Column(name = "name")
    private String name;
    @Column(name = "type")
    private String type;
    @Column(name = "size")
    private float size;
    @Column(name = "owner")
    private String owner;
    @Column(name = "location")
    private String location;
    @Column(name = "state")
    private int state;

    @Builder
    public FileServerPrivateEntity(FileServerPrivateDto dto){
        this.path = dto.getPath();
        this.name = dto.getName();
        this.type = dto.getType();
        this.size = dto.getSize();
        this.owner = dto.getOwner();
        this.location = dto.getLocation();
        this.state = dto.getState();
        this.uuidName = dto.getUuidName();
    }

    public void changePathAndLocation(String path, String location){
        this.path = path;
        this.location = location;
    }
}
