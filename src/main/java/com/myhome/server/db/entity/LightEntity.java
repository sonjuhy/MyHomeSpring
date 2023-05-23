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
@Table(name = "LIGHT_ROOM_TB")
@ToString
@NoArgsConstructor
public class LightEntity {
    @Id
    @Column(name = "LIGHT_ROOM_PK")
    private String room;
    @Column(name = "STATE_CHAR")
    private String state;
    @Column(name = "ROOMKOR_CHAR")
    private String kor;
    @Column(name = "CATEGORY_CHAR")
    private String category;
    @Column(name = "CONNECT_CHAR")
    private String connect;
}
