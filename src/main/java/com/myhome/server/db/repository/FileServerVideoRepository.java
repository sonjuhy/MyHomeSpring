package com.myhome.server.db.repository;

import com.myhome.server.db.entity.FileServerVideoEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileServerVideoRepository extends JpaRepository<FileServerVideoEntity, Integer> {
    List<FileServerVideoEntity> findAllBy(Pageable pageable);
}
