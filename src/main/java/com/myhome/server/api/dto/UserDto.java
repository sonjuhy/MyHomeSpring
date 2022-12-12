package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserDto {
    private long userId;
    private String name;
    private String id;
    private String password;
    private String accessToken;
    private String refreshToken;
    private String auth;

    public List<String> getAuthList(){
        List<String> list = new ArrayList<>();
        String[] strings = this.auth.split(",");
        System.out.println("getAuthList : "+this.auth);
        Collections.addAll(list, strings);
        return list;
    }

}
