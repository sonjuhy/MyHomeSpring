package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LightDto;
import com.myhome.server.api.dto.LightReserveDto;
import com.myhome.server.api.service.*;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.LightEntity;
import com.myhome.server.db.entity.LightReserveEntity;
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
    LightService lightService;

    @Autowired
    LightReserveService lightReserveService;

    @Autowired
    UserService userService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getAllList")
    public ResponseEntity<List<LightEntity>> getAllList(){
        List<LightEntity> list = lightService.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getRoomInfoList/{category}")
    public ResponseEntity<List<LightEntity>> getRoomInfoList(@PathVariable String category){
        List<LightEntity> list = lightService.findByCategory(category);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getRoomInfo/{room}")
    public ResponseEntity<LightEntity> getRoomInfo(@PathVariable String room){
        LightEntity entity = lightService.findByRoom(room);
        if(entity != null) return new ResponseEntity<>(entity, HttpStatus.OK);
        else return new ResponseEntity<>(entity, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getReserve/{pk}")
    public ResponseEntity<LightReserveEntity> getReserve(@PathVariable int pk){
        LightReserveEntity entity = lightReserveService.findByPk(pk);
        if(entity != null) return new ResponseEntity<>(entity, HttpStatus.OK);
        else return new ResponseEntity<>(entity, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getReserveRoom/{room}")
    public ResponseEntity<List<LightReserveEntity>> getReserveRoom(@PathVariable String room){
        List<LightReserveEntity> list = lightReserveService.findByRoom(room);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getReserveAll")
    public ResponseEntity<List<LightReserveEntity>> getReserveAll(){
        List<LightReserveEntity> list = lightReserveService.findAll();
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/control/{accessToken}")
    public ResponseEntity<String> control(@RequestBody LightDto dto, @PathVariable String accessToken){
        String userPK = jwtTokenProvider.getUserPk(accessToken);
        if(userPK == null) return new ResponseEntity<>("no data about token", HttpStatus.UNAUTHORIZED);

        Optional<UserEntity> entity = userService.findById(userPK);
        if(entity.isEmpty()) return new ResponseEntity<>("no data in user pool", HttpStatus.FORBIDDEN);

        lightService.control(dto, entity.get().getName());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/saveReserve")
    public ResponseEntity<Void> saveReserve(@RequestBody LightReserveDto dto){
        lightReserveService.save(dto);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PutMapping("/updateReserve")
    public ResponseEntity<Void> updateReserve(@RequestBody LightReserveDto dto){
        lightReserveService.updateReserve(dto);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @DeleteMapping("/deleteReserve")
    public ResponseEntity<Void> deleteReserve(@RequestParam int pk){
        lightReserveService.deleteReserve(pk);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
