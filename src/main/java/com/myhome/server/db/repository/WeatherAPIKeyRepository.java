package com.myhome.server.db.repository;

import com.myhome.server.db.entity.WeatherAPIKeyEntity;
import com.myhome.server.db.entity.WeatherKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherAPIKeyRepository extends JpaRepository<WeatherAPIKeyEntity, Integer> {
    WeatherAPIKeyEntity findByServiceName(String serviceNameChar);
}
