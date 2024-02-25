package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "WEATHER_TB")
@ToString
@NoArgsConstructor
public class WeatherKeyEntity {

    @Id
    @Column(name = "WEATHER_PK")
    long weatherPk;
    @Column(name = "KEY_CHAR")
    String key;
    @Column(name = "ULTRANCST_CHAR")
    String ultraNcst;
    @Column(name = "ULTRAFCST_CHAR")
    String ultraFcst;
    @Column(name = "VILAGEFCST_CHAR")
    String vilageFcst;

}
