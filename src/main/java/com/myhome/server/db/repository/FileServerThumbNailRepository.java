package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerThumbNailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FileServerThumbNailRepository extends JpaRepository<FileServerThumbNailEntity, String> {
    FileServerThumbNailEntity findByUuid(String uuid);
    List<FileServerThumbNailEntity> findByType(String type);
    void deleteByUuid(String uuid);
    boolean existsByUuid(String uuid);

    // SELECT * FROM FILE_THUMBNAIL_TB WHERE NOT EXISTS(SELECT * FROM FILE_PUBLIC_TB WHERE FILE_PUBLIC_TB.UUID_PK = FILE_THUMBNAIL_TB.UUID_PK);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "SELECT * FROM FILE_THUMBNAIL_TB WHERE NOT EXISTS(SELECT * FROM FILE_PUBLIC_TB WHERE FILE_PUBLIC_TB.UUID_CHAR = FILE_THUMBNAIL_TB.UUID_PK)", nativeQuery = true)
    List<FileServerThumbNailEntity> findAllNotInPublic();

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "SELECT * FROM FILE_THUMBNAIL_TB WHERE NOT EXISTS(SELECT * FROM FILE_PRIVATE_TB WHERE FILE_PRIVATE_TB.UUID_CHAR = FILE_THUMBNAIL_TB.UUID_PK)", nativeQuery = true)
    List<FileServerThumbNailEntity> findAllNotInPrivate();
}
