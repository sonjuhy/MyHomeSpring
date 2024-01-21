package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "WEATHER_KEY_TB")
@ToString
@NoArgsConstructor
public class WeatherAPIKeyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PK")
    int id;
    @Column(name = "SERVICE_NAME_CHAR")
    String serviceName;
    @Column(name = "KEY_CHAR")
    String key;
}
