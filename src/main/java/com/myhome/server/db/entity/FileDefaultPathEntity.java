package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileDefaultPathDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "FILE_DEFAULT_PATH_TB")
@NoArgsConstructor
public class FileDefaultPathEntity {
    @Id
    @Column(name = "ID")
    private long id;
    @Column(name = "PATH_NAME")
    private String pathName;
    @Column(name = "PUBLIC_DEFAULT_PATH_CHAR")
    private String publicDefaultPath;
    @Column(name = "PRIVATE_DEFAULT_PATH_CHAR")
    private String privateDefaultPath;

    @Builder
    public FileDefaultPathEntity(long id, String pathName, String publicDefaultPath, String privateDefaultPath) {
        this.id = id;
        this.pathName = pathName;
        this.publicDefaultPath = publicDefaultPath;
        this.privateDefaultPath = privateDefaultPath;
    }

    @Builder
    public FileDefaultPathEntity(FileDefaultPathDto dto){
        this.id = dto.getId();
        this.pathName = dto.getPathName();
        this.publicDefaultPath = dto.getPublicDefaultPath();
        this.privateDefaultPath = dto.getPrivateDefaultPath();
    }
}
