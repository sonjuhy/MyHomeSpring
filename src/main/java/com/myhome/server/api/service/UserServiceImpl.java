package com.myhome.server.api.service;

import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    UserRepository repository;

    @Override
    public UserEntity findByFnumber(long fnumber) {
        UserEntity entity = repository.findByFnumber(fnumber);
        return entity;
    }
}
