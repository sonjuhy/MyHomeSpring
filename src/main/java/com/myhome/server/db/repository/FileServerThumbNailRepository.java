package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerThumbNailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileServerThumbNailRepository extends JpaRepository<FileServerThumbNailEntity, String> {
    FileServerThumbNailEntity findByUuid(String uuid);
    void deleteByUuid(String uuid);
}
