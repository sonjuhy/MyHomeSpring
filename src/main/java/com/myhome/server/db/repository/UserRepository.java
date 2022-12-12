package com.myhome.server.db.repository;

import com.myhome.server.db.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Optional;

public interface UserRepository extends JpaRepository <UserEntity, Integer>{
    UserEntity findByUserId(long userId);
    Optional<UserEntity> findById(String id);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update User set access_token=:accessToken, refresh_token=:refreshToken where id=:Id", nativeQuery = true)
    void updateTokens(@Param("accessToken") String accessToken, @Param("refreshToken") String refreshToken, @Param("Id") String Id);
//    int save(UserEntity entity);
}
