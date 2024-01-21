package com.myhome.server.api.dto.SGISDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SGISAddressItemDto {

    @JsonProperty(value = "y_coor")
    private int yCoordinate;
    @JsonProperty(value = "full_addr")
    private String fullAddress;
    @JsonProperty(value = "x_coor")
    private int xCoordinate;
    @JsonProperty(value = "addr_name")
    private String AddressName;
    private int cd;
}
