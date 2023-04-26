package com.myhome.server.api.controller;

import com.myhome.server.api.service.WOLService;
import com.myhome.server.api.service.WOLServiceImpl;
import com.myhome.server.db.entity.ComputerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wol")
public class WOLController {

    @Autowired
    private WOLService wolService = new WOLServiceImpl();

    @GetMapping("/wake/{name}")
    public ResponseEntity<Void> wake(@PathVariable String name){
        ComputerEntity computerEntity = wolService.getComputerInfo(name);
        wolService.wake(computerEntity.getMacAddress());
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/ping/{name}/{wait}")
    public ResponseEntity<Boolean> ping(@PathVariable String name, @PathVariable int wait){
        ComputerEntity computerEntity = wolService.getComputerInfo(name);
        boolean result = wolService.ping(computerEntity.getIpAddress(), wait);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/getComputerNameList")
    public ResponseEntity<List<String>> getComputerNameList(){
        List<String> list = wolService.getComputerNameList();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
