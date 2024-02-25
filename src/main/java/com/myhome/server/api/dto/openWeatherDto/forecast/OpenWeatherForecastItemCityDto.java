package com.myhome.server.api.dto.openWeatherDto.forecast;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemCityDto {
    private int id;
    private String name;
    private OpenWeatherForecastItemCoordinateDto coord;
    private String country;
    private int population;
    private int timezone;
    private int sunrise;
    private int sunset;
}
