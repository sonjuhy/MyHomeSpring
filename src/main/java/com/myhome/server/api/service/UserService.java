package com.myhome.server.api.service;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.db.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserEntity findByUserId(long userId);
    Optional<UserEntity> findById(String id);
    List<UserEntity> findAll();
    int updateUser(UserDto userDto);
    void updateTokens(String accessToken, String refreshToken, String Id);

    boolean checkPassword(String inputPassword, String email);

    String getAccessToken(String refreshToken);
    UserEntity getUserInfo(String accessToken);
    boolean validateAccessToken(String accessToken);
    boolean validateRefreshToken(String refreshToken);
    String validateAuth(String accessToken);
    String reissueAccessToken(String accessToken);
    String reissueRefreshToken(String refreshToken);
    String signIn(UserDto dto);
    String signUp(UserDto dto);

}
