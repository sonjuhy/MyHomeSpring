package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileDefaultPathEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDefaultPathRepository extends JpaRepository<FileDefaultPathEntity, Long> {
    FileDefaultPathEntity findByPathName(String pathName);
}
