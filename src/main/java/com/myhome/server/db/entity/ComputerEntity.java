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
@Table(name = "Computer")
@ToString
@NoArgsConstructor
public class ComputerEntity {
    @Id
    @Column(name = "id")
    private int id;
    @Column(name="computer_name")
    private String computerName;
    @Column(name = "mac_address")
    private String macAddress;
    @Column(name = "ip_address")
    private String ipAddress;

}
