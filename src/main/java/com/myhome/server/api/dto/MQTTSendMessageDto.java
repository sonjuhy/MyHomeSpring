package com.myhome.server.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MQTTSendMessageDto {
    private String sender;
    private String message;
    private String destination;
    private String room;
}
