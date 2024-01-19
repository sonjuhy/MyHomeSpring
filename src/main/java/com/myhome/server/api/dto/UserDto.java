package com.myhome.server.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {
    @Schema(description = "DB에서 id 값. 프론트에서 신경안써도 됨", defaultValue = "0")
    private long userId;
    @Schema(description = "계정 ID", defaultValue = "")
    private String id;
    @Schema(description = "계정 이름", defaultValue = "")
    private String name;
    @Schema(description = "계정 pw", defaultValue = "")
    private String password;
    @Schema(description = "계정 accessToken", defaultValue = "")
    private String accessToken;
    @Schema(description = "계정 refreshToken", defaultValue = "")
    private String refreshToken;
    @Schema(description = "계정 권한", defaultValue = "")
    private String auth;
}
