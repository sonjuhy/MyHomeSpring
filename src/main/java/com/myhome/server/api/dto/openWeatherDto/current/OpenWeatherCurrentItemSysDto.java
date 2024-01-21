package com.myhome.server.api.dto.openWeatherDto.current;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentItemSysDto {
    private int type;
    private int id;
    private String country;
    private int sunrise;
    private int sunset;
}
