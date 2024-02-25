package com.myhome.server.api.dto;

import com.myhome.server.db.entity.LightReserveEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LightReserveDto {
    private int pk;
    private String name;
    private String roomKor;
    private String time;
    private String room;
    private String action;
    private String day;
    private String activated;
    private String reiteration;
    private boolean holiday;

    public LightReserveEntity toEntity(){
        return LightReserveEntity.builder()
                .pk(this.pk)
                .name(this.name)
                .roomKor(this.roomKor)
                .time(this.time)
                .room(this.room)
                .action(this.action)
                .day(this.day)
                .activated(this.activated)
                .reiteration(this.reiteration)
                .holiday(this.holiday)
                .build();
    }
}
