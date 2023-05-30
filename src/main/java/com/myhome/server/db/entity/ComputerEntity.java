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
@Table(name = "COMPUTER_TB")
@ToString
@NoArgsConstructor
public class ComputerEntity {
    @Id
    @Column(name = "COMPUTER_PK")
    private int id;
    @Column(name="NAME_CHAR")
    private String name;
    @Column(name = "MAC_CHAR")
    private String macAddress;
    @Column(name = "IP_CHAR")
    private String ipAddress;

}
