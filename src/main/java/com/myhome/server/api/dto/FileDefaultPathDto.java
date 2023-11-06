package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileDefaultPathDto {
    long id;
    String pathName;
    String publicDefaultPath;
    String privateDefaultPath;

    @Builder
    public FileDefaultPathDto(long id, String pathName, String publicDefaultPath, String privateDefaultPath) {
        this.id = id;
        this.pathName = pathName;
        this.publicDefaultPath = publicDefaultPath;
        this.privateDefaultPath = privateDefaultPath;
    }
}
