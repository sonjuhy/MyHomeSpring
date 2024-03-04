package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightReserveDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.LightReserveEntity;
import com.myhome.server.db.repository.LightReserveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LightReserveServiceImpl implements LightReserveService{

    private final String TOPIC_RESERVE_LOG = "reserve-log-topic";

    @Autowired
    LogComponent logComponent;

    @Autowired
    LightReserveRepository repository;

    @Autowired
    KafkaProducer producer;

    @Override
    public LightReserveEntity findByPk(int pk) {
        LightReserveEntity entity = repository.findByPk(pk);
        if(entity != null){
            logComponent.sendLog("Reserve", "[findByPk] find reserve entity (entity) : "+entity+", (pk) : " + pk, true, TOPIC_RESERVE_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[findByPk] entity is null (pk) : " + pk, false, TOPIC_RESERVE_LOG);
        }
        return entity;
    }

    @Override
    public List<LightReserveEntity> findByRoom(String room) {
        List<LightReserveEntity> list = repository.findByRoom(room);
        if(list != null){
            logComponent.sendLog("Reserve", "[findByRoom] find by room (list size) : "+list.size()+", (room) : " +room, true, TOPIC_RESERVE_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[findByRoom] list is null (room) : " + room, false, TOPIC_RESERVE_LOG);
        }
        return list;
    }

    @Override
    public List<LightReserveEntity> findAll() {
        List<LightReserveEntity> list = repository.findAll();
        if(list != null){
            logComponent.sendLog("Reserve", "[findAll] find all reserve (list size) : "+list.size(), true, TOPIC_RESERVE_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[findAll] list is null", false, TOPIC_RESERVE_LOG);
        }
        return list;
    }

    @Override
    public void save(LightReserveDto dto) {
        if(repository.save(dto.toEntity()) != null){
            producer.sendReserveMessage();
            logComponent.sendLog("Reserve", "[save] db save success (dto) : "+dto, true, TOPIC_RESERVE_LOG);
        }
        else{
            logComponent.sendLog("Reserve", "[save] db save failed : "+dto, false, TOPIC_RESERVE_LOG);
        }
    }

    @Transactional
    @Override
    public void updateReserve(LightReserveDto dto) {
        LightReserveEntity entity = repository.findByPk(dto.getPk());
        entity.updateContent(
                dto.getName(),
                dto.getRoom(),
                dto.getRoomKor(),
                dto.getTime(),
                dto.getAction(),
                dto.getDay(),
                dto.getActivated(),
                dto.getReiteration(),
                dto.isHoliday()
                );
//        entity.builder()
//                .name(dto.getName())
//                .roomKor(dto.getRoomKor())
//                .reiteration(dto.getReiteration())
//                .day(dto.getDay())
//                .action(dto.getAction())
//                .activated(dto.getActivated())
//                .time(dto.getTime())
//                .build();

        producer.sendReserveMessage();
        logComponent.sendLog("Reserve", "[updateReserve] reserve is updated (dto) : "+dto, true, TOPIC_RESERVE_LOG);
    }

    @Override
    public void deleteReserve(int pk) {
        repository.deleteById(pk);
        producer.sendReserveMessage();
        logComponent.sendLog("Reserve", "[deleteReserve] reserve is deleted (pk) : "+pk, true, TOPIC_RESERVE_LOG);
    }
}
