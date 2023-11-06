package com.myhome.server.db.entity;

import com.myhome.server.api.dto.FileServerThumbNailDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@ToString
@Table(name = "FILE_THUMBNAIL_TB")
@NoArgsConstructor
public class FileServerThumbNailEntity {
    @Id
    @Column(name = "UUID_PK")
    private String uuid;
    @Column(name = "PATH_CHAR")
    private String path;
    @Column(name = "ORIGIN_FILENAME_CHAR")
    private String originName;
    @Column(name = "TYPE_CHAR")
    private String type;

    @Builder
    public FileServerThumbNailEntity(FileServerThumbNailDto dto){
        this.uuid = dto.getUuid();
        this.path = dto.getPath();
        this.originName = dto.getOriginName();
        this.type = dto.getType();
    }
}
