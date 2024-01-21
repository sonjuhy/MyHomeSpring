package com.myhome.server.api.dto.openWeatherDto.forecast;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.myhome.server.api.dto.openWeatherDto.forecast.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastItemDto {
    private int dt;
    private OpenWeatherForecastItemMainDto main;
    private List<OpenWeatherForecastItemWeatherDto> weather;
    private OpenWeatherForecastItemCloudDto clouds;
    private OpenWeatherForecastItemWindDto wind;
    private int visibility;
    private int pop;
    private OpenWeatherForecastItemRainDto rain;
    private OpenWeatherForecastItemSysDto sys;
    @JsonProperty(value = "dt_txt")
    private String dtTxt;
}
