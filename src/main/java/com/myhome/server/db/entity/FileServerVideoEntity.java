package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileInfoDto;
import com.myhome.server.api.dto.FileServerThumbNailDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@ToString
@Table(name = "FILE_VIDEO_INFO_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerVideoEntity {
    @Id
    @Column(name = "ID_PK")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "PATH_CHAR")
    private String path;
    @Column(name = "UUID_CHAR")
    private String uuid;
    @Column(name = "NAME_CHAR")
    private String name;
    @Builder
    public FileServerVideoEntity(FileInfoDto dto){
        this.id = 0;
        this.path = dto.getPath();
        this.uuid = dto.getUuid();
        this.name = dto.getName();
    }
}
