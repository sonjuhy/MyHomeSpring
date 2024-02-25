package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerThumbNailDto;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@ToString
@Table(name = "FILE_THUMBNAIL_TB")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileServerThumbNailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PK")
    private long id;
    @Column(name = "UUID_CHAR")
    private String uuid;
    @Column(name = "PATH_CHAR")
    private String path;
    @Column(name = "ORIGIN_FILENAME_CHAR")
    private String originName;
    @Column(name = "TYPE_CHAR")
    private String type;

    @Builder
    public FileServerThumbNailEntity(FileServerThumbNailDto dto){
        this.id = dto.getId();
        this.uuid = dto.getUuid();
        this.path = dto.getPath();
        this.originName = dto.getOriginName();
        this.type = dto.getType();
    }
}
