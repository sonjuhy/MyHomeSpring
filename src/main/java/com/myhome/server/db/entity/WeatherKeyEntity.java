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
@Table(name = "WEATHER_TB")
@ToString
@NoArgsConstructor
public class WeatherKeyEntity {

    @Id
    @Column(name = "WEATHER_PK")
    long idWeatherApi;
    @Column(name = "KEY_CHAR")
    String key;
    @Column(name = "ULTRANCST_CHAR")
    String ultraNcst;
    @Column(name = "ULTRAFCST_CHAR")
    String ultraFcst;
    @Column(name = "VILAGEFCST_CHAR")
    String vilageFcst;

}
