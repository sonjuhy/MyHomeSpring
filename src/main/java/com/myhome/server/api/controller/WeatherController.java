package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;
import com.myhome.server.api.service.WeatherService;
import com.myhome.server.api.service.WeatherServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private WeatherService weatherService = new WeatherServiceImpl();

    @GetMapping("/test")
    public void test(){
        try{
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=8uiEDcNjEfxFOoq%2BIjRY2M7MAEKuW7AwNs9%2FyHFZUqmzm4Ci2hyvtfZdgZ7vGHBI6RjxsgBlnq%2BogcZfanSA%2Bw%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1000", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("XML", "UTF-8")); /*요청자료형식(XML/JSON) Default: XML*/
            urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode("20210628", "UTF-8")); /*‘21년 6월 28일 발표*/
            urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode("0600", "UTF-8")); /*06시 발표(정시단위) */
            urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode("55", "UTF-8")); /*예보지점의 X 좌표값*/
            urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode("127", "UTF-8")); /*예보지점의 Y 좌표값*/
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");
            System.out.println("Response code: " + conn.getResponseCode());
            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();
            System.out.println(sb.toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @PostMapping("/testPost/{mode}")
    public ArrayList<WeatherDto> testPost(@RequestBody LocationDto locationDto, @PathVariable int mode){
        System.out.println("location : "+locationDto+", mode : " + mode);
//        ArrayList<WeatherDto> list = weatherService.getWeather(mode, locationDto);
//        return list;
        return null;
    }

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
}
