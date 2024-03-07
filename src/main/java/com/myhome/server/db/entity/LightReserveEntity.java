package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@Entity
@Table(name = "LIGHT_RESERVE_TB")
@ToString
@NoArgsConstructor
@DynamicUpdate
public class LightReserveEntity {
    @Id
    @Column(name = "LIGHT_RESERVE_PK")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pk;
    @Column(name = "NAME_CHAR")
    private String name;
    @Column(name = "ROOM_CHAR")
    private String room;
    @Column(name = "ROOMKOR_CHAR")
    private String roomKor;
    @Column(name = "TIME_CHAR")
    private String time;
    @Column(name = "DO_CHAR")
    private String action;
    @Column(name = "DAY_CHAR")
    private String day;
    @Column(name = "ACTIVATED_CHAR")
    private String activated;
    @Column(name = "REITERATION_CHAR")
    private String reiteration;
    @Column(name = "HOLIDAY_TINYINT", columnDefinition = "TINYINT(4)")
    private boolean holiday;

    @Builder
    public LightReserveEntity(int pk, String name, String room, String roomKor, String time, String action, String day, String activated, String reiteration, boolean holiday) {
        this.pk = pk;
        this.name = name;
        this.room = room;
        this.roomKor = roomKor;
        this.time = time;
        this.action = action;
        this.day = day;
        this.activated = activated;
        this.reiteration = reiteration;
        this.holiday = holiday;
    }

    public void updateContent(String name, String room, String roomKor, String time, String action, String day, String activated, String reiteration, boolean holiday){
        this.name = name;
        this.room = room;
        this.roomKor = roomKor;
        this.time = time;
        this.action = action;
        this.day = day;
        this.activated = activated;
        this.reiteration = reiteration;
        this.holiday = holiday;
    }
}
