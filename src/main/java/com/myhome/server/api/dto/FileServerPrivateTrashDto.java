package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FileServerPrivateTrashDto {
    private String uuid;
    private String path;
    private String name;
    private String type;
    private float size;
    private String owner;
    private String location;
    private int state;

    @Builder
    public FileServerPrivateTrashDto(String uuid, String path, String name, String type, float size, String owner, String location, int state) {
        this.uuid = uuid;
        this.path = path;
        this.name = name;
        this.type = type;
        this.size = size;
        this.owner = owner;
        this.location = location;
        this.state = state;
    }
}
