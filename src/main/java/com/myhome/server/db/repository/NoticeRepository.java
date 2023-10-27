package com.myhome.server.db.repository;

import com.myhome.server.db.entity.NoticeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findAll();
    List<NoticeEntity> findByWriter(String writer);
    NoticeEntity findTopByOrderByIdDesc();
    int deleteById(int id);
}
