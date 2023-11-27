package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {
    private long userId;
    private String id;
    private String name;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String auth;
}
