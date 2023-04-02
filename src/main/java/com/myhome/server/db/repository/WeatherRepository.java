package com.myhome.server.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository  {
    String getKey();
    void setKey();
}
