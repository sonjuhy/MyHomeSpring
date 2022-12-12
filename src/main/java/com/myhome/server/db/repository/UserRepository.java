package com.myhome.server.db.repository;

import com.myhome.server.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository <UserEntity, Integer>{
    UserEntity findByFnumber(long fnumber);
}
