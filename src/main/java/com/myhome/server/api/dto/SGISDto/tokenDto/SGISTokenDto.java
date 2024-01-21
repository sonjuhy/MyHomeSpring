package com.myhome.server.api.dto.SGISDto.tokenDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SGISTokenDto {
    private String id;
    private SGISTokenResultDto result;
    private String errMsg;
    private int errCd;
    private String trId;
}
