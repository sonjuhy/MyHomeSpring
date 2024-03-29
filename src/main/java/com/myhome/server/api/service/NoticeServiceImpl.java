package com.myhome.server.api.service;

import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.NoticeEntity;
import com.myhome.server.db.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    private final String TOPIC_NOTICE_LOG = "notice-log-topic";

    @Autowired
    LogComponent logComponent;

    @Autowired
    NoticeRepository repository;

    @Autowired
    KafkaProducer producer;

    @Override
    public int save(NoticeEntity entity) {
        if(repository.save(entity) != null){
            logComponent.sendLog("Notice", "[save] db save success (dto) : "+entity, true, TOPIC_NOTICE_LOG);
        }
        else{
            logComponent.sendLog("Notice", "[save] db save failed : "+entity, false, TOPIC_NOTICE_LOG);
        }
        return 0;
    }

    @Override
    public List<NoticeEntity> findAll() {
        List<NoticeEntity> list = repository.findAll();
        if(list != null){
            logComponent.sendLog("Notice", "[findAll] find all notice list (list size) : "+list.size(), true, TOPIC_NOTICE_LOG);
        }
        else{
            logComponent.sendLog("Notice", "[findAll] list is null", false, TOPIC_NOTICE_LOG);
        }
        return list;
    }

    @Override
    public List<NoticeEntity> findByWriter(String writer) {
        List<NoticeEntity> list = repository.findByWriter(writer);
        if(list != null){
            logComponent.sendLog("Notice", "[findByWriter] find notice list by writer (list size) : "+list.size()+", writer : "+writer, true, TOPIC_NOTICE_LOG);
        }
        else{
            logComponent.sendLog("Notice", "[findByWriter] list is null", false, TOPIC_NOTICE_LOG);
        }
        return list;
    }

    @Override
    public NoticeEntity findTopNotice() {
        NoticeEntity entity = repository.findTopByOrderByIdDesc();
        if(entity != null){
            logComponent.sendLog("Notice", "[findTopNotice] find top notice entity (entity) : "+entity, true, TOPIC_NOTICE_LOG);
        }
        else{
            logComponent.sendLog("Notice", "[findTopNotice] entity is null", false, TOPIC_NOTICE_LOG);
        }
        return entity;
    }

    @Override
    public int delete(int id) {
        repository.deleteById(id);
        return 0;
    }
}
