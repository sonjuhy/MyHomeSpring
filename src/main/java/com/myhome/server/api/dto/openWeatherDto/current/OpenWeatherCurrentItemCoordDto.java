package com.myhome.server.api.dto.openWeatherDto.current;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentItemCoordDto {
    private float lon;
    private float lat;
}
