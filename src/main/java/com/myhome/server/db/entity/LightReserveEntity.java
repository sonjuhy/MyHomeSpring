package com.myhome.server.db.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

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
    @Column(name = "NAMEKOR_CHAR")
    private String nameKor;
    @Column(name = "TIME_CHAR")
    private String time;
    @Column(name = "ROOM_CHAR")
    private String room;
    @Column(name = "DO_CHAR")
    private String action;
    @Column(name = "DAY_CHAR")
    private String day;
    @Column(name = "ACTIVATED_CHAR")
    private String activated;
    @Column(name = "REITERATION_CHAR")
    private String reiteration;

    @Builder
    protected LightReserveEntity(int pk, String name, String nameKor, String time, String room, String action, String day, String activated, String reiteration) {
        this.pk = pk;
        this.name = name;
        this.nameKor = nameKor;
        this.time = time;
        this.room = room;
        this.action = action;
        this.day = day;
        this.activated = activated;
        this.reiteration = reiteration;
    }
}
