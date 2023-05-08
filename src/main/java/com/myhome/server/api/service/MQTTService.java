package com.myhome.server.api.service;

import com.myhome.server.api.dto.MQTTSendMessageDto;

public interface MQTTService {
    void publish(MQTTSendMessageDto dto);
    boolean connected();
    void reconnect();
}
