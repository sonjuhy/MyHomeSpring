package com.myhome.server.db.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "APP_VERSION")
@ToString
@NoArgsConstructor
public class ApkEntity {
    @Id
    @Column(name = "ID")
    private int id;
    @Column(name = "APP_VERSION")
    private double version;
    @Column(name = "UPLOAD_DATE")
    private String date;
}
