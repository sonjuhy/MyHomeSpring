package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LightDto {
    private String room;
    private String state;
    private String kor;
    private String category;
    private String connect;
}
