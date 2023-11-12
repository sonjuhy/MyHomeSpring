package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightReserveDto;
import com.myhome.server.db.entity.LightReserveEntity;
import com.myhome.server.db.repository.LightReserveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightReserveServiceImpl implements LightReserveService{

    private final String TOPIC_RESERVE_LOG = "reserve-log-topic";

    @Autowired
    LightReserveRepository repository;

    @Autowired
    KafkaProducer producer;

    @Override
    public LightReserveEntity findByPk(int pk) {
        LightReserveEntity entity = repository.findByPk(pk);
        if(entity != null){
            producer.sendLogDto("Reserve", "[findByPk] find reserve entity (entity) : "+entity+", (pk) : " + pk, true, TOPIC_RESERVE_LOG);
        }
        else{
            producer.sendLogDto("Reserve", "[findByPk] entity is null (pk) : " + pk, false, TOPIC_RESERVE_LOG);
        }
        return entity;
    }

    @Override
    public List<LightReserveEntity> findByRoom(String room) {
        List<LightReserveEntity> list = repository.findByRoom(room);
        if(list != null){
            producer.sendLogDto("Reserve", "[findByRoom] find by room (list size) : "+list.size()+", (room) : " +room, true, TOPIC_RESERVE_LOG);
        }
        else{
            producer.sendLogDto("Reserve", "[findByRoom] list is null (room) : " + room, false, TOPIC_RESERVE_LOG);
        }
        return list;
    }

    @Override
    public List<LightReserveEntity> findAll() {
        List<LightReserveEntity> list = repository.findAll();
        if(list != null){
            producer.sendLogDto("Reserve", "[findAll] find all reserve (list size) : "+list.size(), true, TOPIC_RESERVE_LOG);
        }
        else{
            producer.sendLogDto("Reserve", "[findAll] list is null", false, TOPIC_RESERVE_LOG);
        }
        return list;
    }

    @Override
    public void save(LightReserveDto dto) {
        if(repository.save(dto.toEntity()) != null){
            producer.sendReserveMessage();
            producer.sendLogDto("Reserve", "[save] db save success (dto) : "+dto, true, TOPIC_RESERVE_LOG);
        }
        else{
            producer.sendLogDto("Reserve", "[save] db save failed : "+dto, false, TOPIC_RESERVE_LOG);
        }
    }

    @Override
    public void updateReserve(LightReserveDto dto) {
        LightReserveEntity entity = repository.findByPk(dto.getPk());
        entity.builder()
                .name(dto.getName())
                .roomKor(dto.getRoomKor())
                .reiteration(dto.getReiteration())
                .day(dto.getDay())
                .action(dto.getAction())
                .activated(dto.getActivated())
                .time(dto.getTime())
                .build();
        producer.sendReserveMessage();
        producer.sendLogDto("Reserve", "[updateReserve] reserve is updated (dto) : "+dto, true, TOPIC_RESERVE_LOG);
    }

    @Override
    public void deleteReserve(int pk) {
        repository.deleteById(pk);
        producer.sendReserveMessage();
        producer.sendLogDto("Reserve", "[deleteReserve] reserve is deleted (pk) : "+pk, true, TOPIC_RESERVE_LOG);
    }
}
