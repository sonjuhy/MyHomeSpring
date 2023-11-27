package com.myhome.server.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
