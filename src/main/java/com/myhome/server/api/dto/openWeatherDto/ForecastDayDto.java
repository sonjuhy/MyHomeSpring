package com.myhome.server.api.dto.openWeatherDto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDayDto {
    private String weather;
    private String day;
    private String time;
    private double min;
    private double max;
}
