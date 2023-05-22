package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.api.dto.MQTTSendMessageDto;
import com.myhome.server.api.service.LightService;
import com.myhome.server.api.service.LightServiceImpl;
import com.myhome.server.api.service.MQTTService;
import com.myhome.server.api.service.MQTTServiceImpl;
import com.myhome.server.db.entity.LightEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/light")
public class LightController {

    @Autowired
    MQTTService mqttService = new MQTTServiceImpl();

    @Autowired
    LightService lightService = new LightServiceImpl();

    @PostMapping("/pub")
    public ResponseEntity<Void> publish(@RequestBody MQTTSendMessageDto dto){
        mqttService.publish(dto);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/getAllList")
    public ResponseEntity<List<LightEntity>> getAllList(){
        List<LightEntity> list = lightService.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getRoomInfo/{room}")
    public ResponseEntity<LightEntity> getRoomInfo(@PathVariable String room){
        LightEntity entity = lightService.findByRoom(room);
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PostMapping("/control")
    public ResponseEntity<Void> control(@RequestBody LightDto dto){
        System.out.println(dto);
        lightService.control(dto);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
