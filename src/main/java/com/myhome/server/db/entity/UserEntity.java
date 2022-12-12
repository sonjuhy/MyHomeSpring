package com.myhome.server.db.entity;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "User")
@ToString
public class UserEntity {
    @Id
    @Column(name = "fnumber")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long fnumber;

    @Column(name = "name")
    private String name;
    @Column(name = "ID")
    private String ID;
    @Column(name = "PW")
    private String PW;
}
