package com.myhome.server.api.dto.openWeatherDto.forecast;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemWeatherDto {
    private int id;
    private String main;
    private String description;
    private String icon;
}
