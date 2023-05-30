package com.myhome.server.api.service;

import com.myhome.server.db.entity.NoticeEntity;

import java.util.List;

public interface NoticeService {
    int save(NoticeEntity entity);
    List<NoticeEntity> findAll();
    NoticeEntity findByWriter(String writer);
    NoticeEntity findTopNotice();
}
