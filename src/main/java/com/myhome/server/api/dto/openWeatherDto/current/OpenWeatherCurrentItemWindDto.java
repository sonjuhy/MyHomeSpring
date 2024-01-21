package com.myhome.server.api.dto.openWeatherDto.current;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentItemWindDto {
    private float speed;
    private int deg;
    private float gust;
}
