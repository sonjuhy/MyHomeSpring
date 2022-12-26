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
    @Query(value = "UPDATE fileserver_private SET location=:location WHERE path=:path", nativeQuery = true)
    int updateLocation(@Param("path") String path, @Param("location") String location);
}
