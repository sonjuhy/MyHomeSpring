package com.myhome.server.api.service;

import com.myhome.server.api.dto.LoginDto;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.db.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserEntity findByUserId(long userId);
    UserEntity findByAccessToken(String uuid);
    Optional<UserEntity> findById(String id);
    List<UserEntity> findAll();
    int updateUser(UserDto userDto);
    void updateTokens(String accessToken, String refreshToken, String Id);
    UserEntity getUserInfo(String accessToken);
    String signIn(LoginDto dto);
    String signUp(UserDto dto);

}
