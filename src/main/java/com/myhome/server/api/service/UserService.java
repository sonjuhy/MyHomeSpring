package com.myhome.server.api.service;

import com.myhome.server.db.entity.UserEntity;

public interface UserService {
    UserEntity findByFnumber(long fnumber);
}
