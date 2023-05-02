package com.myhome.server.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Entity
@Table(name = "weather_api")
@ToString
@NoArgsConstructor
public class WeatherKeyEntity {

    @Id
    @Column(name = "idweather_api")
    long idWeatherApi;
    @Column(name = "key")
    String key;
    @Column(name = "ultra_ncst")
    String ultraNcst;
    @Column(name = "ultra_fcst")
    String ultraFcst;
    @Column(name = "vilage_fcst")
    String vilageFcst;

}
