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
@Table(name = "room_light")
@ToString
@NoArgsConstructor
public class LightEntity {
    @Id
    @Column(name = "Room")
    private String room;
    @Column(name = "State")
    private String state;
    @Column(name = "Kor")
    private String kor;
    @Column(name = "category")
    private String category;
    @Column(name = "Connect")
    private String connect;
}
