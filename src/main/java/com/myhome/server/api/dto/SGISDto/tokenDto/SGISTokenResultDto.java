package com.myhome.server.api.dto.SGISDto.tokenDto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SGISTokenResultDto {
    private String accessTimeout;
    private String accessToken;
}
