package com.myhome.server.api.dto.openWeatherDto;

import com.myhome.server.api.dto.openWeatherDto.forecast.OpenWeatherForecastItemCityDto;
import com.myhome.server.api.dto.openWeatherDto.forecast.OpenWeatherForecastItemDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherForecastDto {
    private int cod;
    private int message;
    private int cnt;
    private List<OpenWeatherForecastItemDto> list;
    private OpenWeatherForecastItemCityDto city;
}
