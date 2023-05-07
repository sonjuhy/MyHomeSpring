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
    @Query(value = "DELETE FROM fileserver_private WHERE state=:state", nativeQuery = true)
    int deleteByState(@Param("state") int state);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE fileserver_private SET location=:location WHERE path=:path", nativeQuery = true)
    int updateLocation(@Param("path") String path, @Param("location") String location);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE fileserver_private SET state=0", nativeQuery = true)
    int updateAllStateToZero();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE fileserver_private SET state=1", nativeQuery = true)
    int updateAllStateToOne();
}
