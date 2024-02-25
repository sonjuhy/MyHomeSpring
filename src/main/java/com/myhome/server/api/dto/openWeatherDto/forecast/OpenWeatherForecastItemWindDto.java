package com.myhome.server.api.dto.openWeatherDto.forecast;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemWindDto {
    private float speed;
    private int deg;
    private float gust;
}
