package com.myhome.server.api.dto.SGISDto.geoCodeDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeoCodeItemResultDto {
    private double posY;
    private double posX;
}
