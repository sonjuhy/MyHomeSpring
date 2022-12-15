package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerPublicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerPublicRepository extends JpaRepository<FileServerPublicEntity, String> {
//    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    FileServerPublicEntity findByPath(String path);
    List<FileServerPublicEntity> findByLocation(String location);
    boolean existByPath(String path);
    @Transactional
    long deleteByPath(String path);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE fileserver_public SET location=:location WHERE path=:path", nativeQuery = true)
    int updateLocation(@Param("path") String path, @Param("location") String location);
}
