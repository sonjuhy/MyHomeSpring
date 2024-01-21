package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.SGISDto.SGISAddressDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherCurrentDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherForecastDto;
import com.myhome.server.api.dto.WeatherDto;
import com.myhome.server.api.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    /*
    * OpenWeatherAPI
    * */
    @Operation(description = "현재 날씨 정보 얻는 API. lat : 위도, lon : 경도")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "null 일 경우 백엔드 에러. 그 외 정상 처리 자세한 정보는 https://openweathermap.org/current 참조")
    })
    @GetMapping("/getCurrentInfo")
    public ResponseEntity<OpenWeatherCurrentDto> getCurrentInfo(@RequestParam double lat, @RequestParam double lon){
        OpenWeatherCurrentDto dto = weatherService.getCurrentWeatherInfo(lat, lon);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(description = "5일간 3시간 간격 날씨 데이터 API. lat : 위도, lon : 경도")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "null 일 경우 백엔드 에러. 그 외 정상 처리 자세한 정보는 https://openweathermap.org/current 참조")
    })
    @GetMapping("/getForecastInfo")
    public ResponseEntity<OpenWeatherForecastDto> getForecastInfo(@RequestParam double lat, @RequestParam double lon){
        OpenWeatherForecastDto dto = weatherService.getForecastWeatherInfo(lat, lon);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(description = "도시의 x, y 좌표를 경도, 위도로 변경하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "null 일 경우 백엔드 에러. 그 외 정상 처리")
    })
    @GetMapping("/getSidoInfo/{num}")
    public ResponseEntity<SGISAddressDto> getSidoInfo(@PathVariable int num){
        SGISAddressDto dto = weatherService.getSGISAddressInfo(num);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(description = "도시의 x, y 좌표를 이용하여 현재 날씨 데이터 얻는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "null 일 경우 백엔드 에러. 그 외 정상 처리 자세한 정보는 https://openweathermap.org/current 참조")
    })
    @GetMapping("/getCurrentInfoByCoordinate/{x}/{y}")
    public ResponseEntity<OpenWeatherCurrentDto> getCurrentInfoByCoordinate(@PathVariable int x, @PathVariable int y){
        OpenWeatherCurrentDto dto = weatherService.getCurrentWeatherInfoByCoordinate(x, y);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(description = "도시의 x, y 좌표를 이용하여 5일간 3시간 데이터 얻는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "null 일 경우 백엔드 에러. 그 외 정상 처리 자세한 정보는 https://openweathermap.org/current 참조")
    })
    @GetMapping("/getForecastInfoByCoordinate/{x}/{y}")
    public ResponseEntity<OpenWeatherForecastDto> getForecastInfoByCoordinate(@PathVariable int x, @PathVariable int y){
        OpenWeatherForecastDto dto = weatherService.getForecastWeatherInfoByCoordinate(x, y);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    /*
    * 기상청 API
    * */
    @GetMapping("/getTopPlace")
    public ResponseEntity<ArrayList<LocationDto>> getTopPlace(){
        ArrayList<LocationDto> list = weatherService.getTopPlace();
        if(list != null && !list.isEmpty()) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getMiddlePlace/{code}")
    public ResponseEntity<ArrayList<LocationDto>> getMiddlePlace(@PathVariable String code){
        ArrayList<LocationDto> list = weatherService.getMiddlePlace(code);
        if(list != null && !list.isEmpty()) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }
    @GetMapping("/getLeafPlace/{code}")
    public ResponseEntity<ArrayList<LocationDto>> getLeafPlace(@PathVariable String code){
        ArrayList<LocationDto> list = weatherService.getLeafPlace(code);
        if(list != null && !list.isEmpty()) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/getUltraNcst")
    public ResponseEntity<WeatherDto> getUltraNcst(@RequestBody LocationDto locationDto){
        WeatherDto dto = weatherService.getUtlraNcst(locationDto);

        if(dto != null) return new ResponseEntity<>(dto, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/getUltraFcst")
    public ResponseEntity<ArrayList<WeatherDto>> getUltraFcst(@RequestBody LocationDto locationDto){
        ArrayList<WeatherDto> list = weatherService.getUtlraFcst(locationDto);

        if(list != null && !list.isEmpty()) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/getVilageFcst")
    public ResponseEntity<ArrayList<WeatherDto>> getVilageFcst(@RequestBody LocationDto locationDto){
        ArrayList<WeatherDto> list = weatherService.getVilageFcst(locationDto);
        if(list != null && !list.isEmpty()) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }


}
