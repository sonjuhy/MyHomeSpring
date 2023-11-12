package com.myhome.server.api.dto;

import lombok.*;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogReceiveDto {
    private long id;
    private boolean type; // error(false), success(true)
    private String sender; // spring, django
    private String service; // light(iot), reserve(iot), cloud, fileCheck(cloud), notice, weather
    private String content; // log content
}
