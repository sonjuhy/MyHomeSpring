package com.myhome.server.api.dto.openWeatherDto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemRainDto {
    @JsonProperty(value = "3h")
    private double threeHour;
}
