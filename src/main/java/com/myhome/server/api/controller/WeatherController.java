package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;
import com.myhome.server.api.service.WeatherService;
import com.myhome.server.api.service.WeatherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService = new WeatherServiceImpl();

    @PostMapping("/getUltraNcst")
    public ResponseEntity<WeatherDto> getUltraNcst(@RequestBody LocationDto locationDto){
        System.out.println("getUltraNcst location : " + locationDto);
        WeatherDto dto = weatherService.getUtlraNcst(locationDto);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @PostMapping("/getUltraFcst")
    public ResponseEntity<ArrayList<WeatherDto>> getUltraFcst(@RequestBody LocationDto locationDto){
        System.out.println("getUltraFcst location : " + locationDto);
        ArrayList<WeatherDto> list = weatherService.getUtlraFcst(locationDto);

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/getVilageFcst")
    public ResponseEntity<ArrayList<WeatherDto>> getVilageFcst(@RequestBody LocationDto locationDto){
        System.out.println("getVilageFcst location : " + locationDto);
        ArrayList<WeatherDto> list = weatherService.getVilageFcst(locationDto);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getTopPlace")
    public ResponseEntity<ArrayList<LocationDto>> getTopPlace(){
        ArrayList<LocationDto> list = weatherService.getTopPlace();
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/getMiddlePlace/{code}")
    public ResponseEntity<ArrayList<LocationDto>> getMiddlePlace(@PathVariable String code){
        ArrayList<LocationDto> list = weatherService.getMiddlePlace(code);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.OK);
    }
    @GetMapping("/getLeafPlace/{code}")
    public ResponseEntity<ArrayList<LocationDto>> getLeafPlace(@PathVariable String code){
        ArrayList<LocationDto> list = weatherService.getLeafPlace(code);
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.OK);
    }

}
