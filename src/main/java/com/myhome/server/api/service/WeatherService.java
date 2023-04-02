package com.myhome.server.api.service;

import com.google.gson.JsonArray;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;

import java.util.ArrayList;

public interface WeatherService {
    String getKey();
    String getLinkUltraNcst();
    String getLinkUltraFcst();
    String getLinkVilageFcst();
    String calVEC(String value);
    ArrayList<WeatherDto> JsonParsing(JsonArray jsonArray, int mode);
    JsonArray fnJson(String Data);
    ArrayList<WeatherDto> getUtlraNcst(LocationDto locationDto); // 초단기실황조회
    ArrayList<WeatherDto> getUtlraFcst(LocationDto locationDto); // 초단기예보조회
    ArrayList<WeatherDto> getVilageFcst(LocationDto locationDto); // 단기예보조회
    String ApiTime();
    String ApiTimeChange(String time);
}
