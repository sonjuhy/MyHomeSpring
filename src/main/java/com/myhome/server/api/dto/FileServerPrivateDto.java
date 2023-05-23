package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
@Getter
@Setter
@NoArgsConstructor
public class FileServerPrivateDto {
    private String path;
    private String name;
    private String uuidName;
    private String type;
    private float size;
    private String owner;
    private String location;
    private int state;
}
