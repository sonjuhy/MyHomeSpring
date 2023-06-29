package com.myhome.server.api.service;

import com.myhome.server.api.dto.NoticeDto;
import com.myhome.server.db.entity.NoticeEntity;
import com.myhome.server.db.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    NoticeRepository repository;

    @Override
    public int save(NoticeEntity entity) {
        repository.save(entity);
        return 0;
    }

    @Override
    public List<NoticeEntity> findAll() {
        List<NoticeEntity> list = repository.findAll();
        return list;
    }

    @Override
    public List<NoticeEntity> findByWriter(String writer) {
        List<NoticeEntity> list = repository.findByWriter(writer);
        return list;
    }

    @Override
    public NoticeEntity findTopNotice() {
        NoticeEntity entity = repository.findTopByOrderByIdDesc();
        return entity;
    }

    @Override
    public int delete(int id) {
        repository.deleteById(id);
        return 0;
    }
}
