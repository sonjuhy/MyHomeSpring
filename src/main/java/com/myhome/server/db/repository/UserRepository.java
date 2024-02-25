package com.myhome.server.db.repository;

import com.myhome.server.db.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository <UserEntity, Long>{
    UserEntity findByUserId(long Id);
    Optional<UserEntity> findById(String userId);
    Optional<UserEntity> findByAccessToken(String accessToken);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update USER_TB set ACCESS_CHAR=:accessToken, REFRESH_CHAR=:refreshToken where ID_CHAR=:Id", nativeQuery = true)
    void updateTokens(@Param("accessToken") String accessToken, @Param("refreshToken") String refreshToken, @Param("Id") String Id);
//    int save(UserEntity entity);
}
