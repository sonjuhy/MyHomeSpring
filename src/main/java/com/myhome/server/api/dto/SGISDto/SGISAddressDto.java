package com.myhome.server.api.dto.SGISDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SGISAddressDto {
    private String id;
    private List<SGISAddressItemDto> result;
}
