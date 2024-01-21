package com.myhome.server.api.dto.openWeatherDto.current;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentItemWeatherDto {
    private int id;
    private String main;
    private String description;
    private String icon;
}
