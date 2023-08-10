package com.myhome.server.api.service;

import com.myhome.server.api.dto.LightReserveDto;
import com.myhome.server.db.entity.LightReserveEntity;
import com.myhome.server.db.repository.LightReserveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LightReserveServiceImpl implements LightReserveService{

    @Autowired
    LightReserveRepository repository;

    @Autowired
    KafkaProducer producer;

    @Override
    public LightReserveEntity findByPk(int pk) {
        LightReserveEntity entity = repository.findByPk(pk);
        return entity;
    }

    @Override
    public List<LightReserveEntity> findByRoom(String room) {
        List<LightReserveEntity> list = repository.findByRoom(room);
        return list;
    }

    @Override
    public List<LightReserveEntity> findAll() {
        List<LightReserveEntity> list = repository.findAll();
        return list;
    }

    @Override
    public void save(LightReserveDto dto) {
        repository.save(dto.toEntity());
        producer.sendReserveMessage();
    }

    @Override
    public void updateReserve(LightReserveDto dto) {
        LightReserveEntity entity = repository.findByPk(dto.getPk());
        entity.builder()
                .name(dto.getName())
                .nameKor(dto.getNameKor())
                .reiteration(dto.getReiteration())
                .day(dto.getDay())
                .action(dto.getAction())
                .activated(dto.getActivated())
                .time(dto.getTime())
                .build();
    }

    @Override
    public void deleteReserve(int pk) {
        repository.deleteById(pk);
        producer.sendReserveMessage();
    }
}
