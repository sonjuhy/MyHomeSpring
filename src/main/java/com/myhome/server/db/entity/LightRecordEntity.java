package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "LIGHT_RECORD_TB")
@ToString
@NoArgsConstructor
public class LightRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LIGHT_RECORD_PK")
    private int pk;
    @Column(name = "ROOM_CHAR")
    private String room;
    @Column(name = "TIME_CHAR")
    private String time;
    @Column(name = "DAY_CHAR")
    private String day;
    @Column(name = "DO_CHAR")
    private String action;
    @Column(name = "USER_CHAR")
    private String user;
}
