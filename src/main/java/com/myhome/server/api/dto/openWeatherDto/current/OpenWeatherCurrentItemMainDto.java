package com.myhome.server.api.dto.openWeatherDto.current;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OpenWeatherCurrentItemMainDto {
    private float temp;
    private float fellsLike;
    private float tempMin;
    private float tempMax;
    private int pressure;
    private int humidity;
    private int seaLevel;
    private int grndLevel;
}
