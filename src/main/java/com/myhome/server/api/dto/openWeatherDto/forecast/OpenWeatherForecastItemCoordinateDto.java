package com.myhome.server.api.dto.openWeatherDto.forecast;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemCoordinateDto {
    private double lat;
    private double lon;
}
