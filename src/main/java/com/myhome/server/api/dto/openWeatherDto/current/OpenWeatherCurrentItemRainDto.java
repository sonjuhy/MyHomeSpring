package com.myhome.server.api.dto.openWeatherDto.current;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenWeatherCurrentItemRainDto {
    @JsonProperty(value = "1h")
    private float hour;
}
