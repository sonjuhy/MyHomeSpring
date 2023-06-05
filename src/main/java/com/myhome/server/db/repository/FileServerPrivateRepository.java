package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPrivateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPrivateRepository extends JpaRepository<FileServerPrivateEntity, String> {
    FileServerPrivateEntity findByPath(String path);

    List<FileServerPrivateEntity> findByLocation(String location);

    List<FileServerPrivateEntity> findByOwner(String owner);

    boolean existsByPath(String path);

    @Transactional
    long deleteByPath(String path);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM FILE_PRIVATE_TB WHERE STATE_INT=:state", nativeQuery = true)
    int deleteByState(@Param("state") int state);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PRIVATE_TB SET LOCATION_CHAR=:location WHERE PATH_CHAR=:path", nativeQuery = true)
    int updateLocation(@Param("path") String path, @Param("location") String location);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PRIVATE_TB SET STATE_INT=0", nativeQuery = true)
    int updateAllStateToZero();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE FILE_PRIVATE_TB SET STATE_INT=1", nativeQuery = true)
    int updateAllStateToOne();
}
