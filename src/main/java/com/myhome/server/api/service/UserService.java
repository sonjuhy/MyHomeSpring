package com.myhome.server.api.service;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.db.entity.UserEntity;

import java.util.Optional;

public interface UserService {
    UserEntity findByUserId(long userId);
    Optional<UserEntity> findById(String id);
    int updateUser(UserDto userDto);
    void updateTokens(String accessToken, String refreshToken, String Id);

    boolean checkPassword(String inputPassword, String email);
}
