package com.myhome.server.api.dto.SGISDto.geoCodeDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GeoCodeDto {
    private String id;
    private GeoCodeItemResultDto result;
    private String errMsg;
    private int errCd;
    private String trId;
}
