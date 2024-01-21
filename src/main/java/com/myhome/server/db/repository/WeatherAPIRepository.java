package com.myhome.server.db.repository;

import com.myhome.server.db.entity.WeatherAPIEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherAPIRepository extends JpaRepository<WeatherAPIEntity, String> {
    WeatherAPIEntity findByServiceName(String service);
}
