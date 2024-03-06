package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Entity
@ToString
@Table(name = "FILE_THUMBNAIL_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerVideoEntity {
    @Id
    @Column(name = "ID_PK")
    private int id;
    @Column(name = "PATH_CHAR")
    private String path;
    @Column(name = "UUID_CHAR")
    private String uuid;
    @Column(name = "NAME_CHAR")
    private String name;
    @Builder
    public FileServerVideoEntity(FileInfoDto dto){
        this.path = dto.getPath();
        this.uuid = dto.getUuid();
        this.name = dto.getName();
    }
}
