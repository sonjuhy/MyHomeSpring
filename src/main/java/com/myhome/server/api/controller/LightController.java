package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.api.dto.MQTTSendMessageDto;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.*;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.LightEntity;
import com.myhome.server.db.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController()
@RequestMapping("/light")
public class LightController {

    @Autowired
    MQTTService mqttService = new MQTTServiceImpl();

    @Autowired
    LightService lightService = new LightServiceImpl();

    @Autowired
    UserService userService = new UserServiceImpl();

    @Autowired
    JwtTokenProvider jwtTokenProvider;

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

    @PostMapping("/control/{accessToken}")
    public ResponseEntity<String> control(@RequestBody LightDto dto, @PathVariable String accessToken){
        String userPK = jwtTokenProvider.getUserPk(accessToken);
        if(userPK == null) return new ResponseEntity<>("no data about token", HttpStatus.OK);

        Optional<UserEntity> entity = userService.findById(userPK);
        if(entity.isEmpty()) return new ResponseEntity<>("no data in user pool", HttpStatus.OK);

        lightService.control(dto, entity.get().getName());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
