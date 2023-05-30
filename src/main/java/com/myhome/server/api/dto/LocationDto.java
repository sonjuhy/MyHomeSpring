package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class LocationDto {
    private String name;
    private String code;
    private int x_code;
    private int y_code;
}
