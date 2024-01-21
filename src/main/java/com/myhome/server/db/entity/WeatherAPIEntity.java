package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@Entity
@Table(name = "WEATHER_API_TB")
@ToString
@NoArgsConstructor
public class WeatherAPIEntity {
    @Id
    @Column(name = "UUID_PK")
    String uuidPk;
    @Column(name = "SERVICE_NAME_CHAR")
    String serviceName;
}
