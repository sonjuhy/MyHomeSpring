package com.myhome.server.api.controller;

import com.myhome.server.api.dto.MQTTSendMessageDto;
import com.myhome.server.api.service.MQTTService;
import com.myhome.server.api.service.MQTTServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/MQTT")
public class MQTTController {

    @Autowired
    MQTTService service = new MQTTServiceImpl();

    @PostMapping("/pub")
    public ResponseEntity<Void> publish(@RequestBody MQTTSendMessageDto dto){
        service.publish(dto);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
