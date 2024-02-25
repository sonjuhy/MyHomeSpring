package com.myhome.server.api.dto.openWeatherDto;

import com.myhome.server.api.dto.openWeatherDto.current.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentDto {
    private OpenWeatherCurrentItemCoordDto coord;
    private List<OpenWeatherCurrentItemWeatherDto> weather;
    private String base;
    private OpenWeatherCurrentItemMainDto main;
    private int visibility;
    private OpenWeatherCurrentItemWindDto wind;
    private OpenWeatherCurrentItemRainDto rain;
    private OpenWeatherCurrentItemSnowDto snow;
    private OpenWeatherCurrentItemCloudDto clouds;
    private int dt;
    private OpenWeatherCurrentItemSysDto sys;
    private int timezone;
    private int id;
    private String name;
    private int cod;
}
