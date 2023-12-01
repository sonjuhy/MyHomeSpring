package com.myhome.server.component;

import com.myhome.server.api.dto.LogReceiveDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

@Component
public class LogComponent {

    @Autowired
    KafkaProducer producer;

    public void sendLog(String service, String content, boolean type, String topic){
        producer.sendLogDto(toDto(service, content, type), topic);
    }

    public void sendErrorLog(String service, String content, Exception e, String topic){
        String error = exceptionToString(e);
        content += error;
        System.out.println(content);
        producer.sendLogDto(toDto(service, content, false), topic);
    }

    private LogReceiveDto toDto(String service, String content, boolean type){
        LogReceiveDto dto = new LogReceiveDto();
        dto.setId(0);
        dto.setSender("Spring");
        dto.setService(service);
        dto.setContent(content);
        dto.setType(type);
        return dto;
    }
    private String exceptionToString(Exception e){
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
