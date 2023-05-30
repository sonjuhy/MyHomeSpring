package com.myhome.server.db.repository;

import com.myhome.server.db.entity.WeatherKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface WeatherRepository extends JpaRepository<WeatherKeyEntity, Integer>, JpaSpecificationExecutor<WeatherKeyEntity> {
    WeatherKeyEntity findByWeatherPk(long id);
}