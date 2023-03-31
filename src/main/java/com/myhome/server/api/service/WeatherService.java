package com.myhome.server.api.service;

import com.google.gson.JsonArray;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;

import java.util.ArrayList;

public interface WeatherService {
    String getKey();
    String calVEC(String value);
    WeatherDto JsonParsing(JsonArray jsonArray, int mode);
    String fnJson(String Data, int mode);
    String getWeatherInfo(String value, String numOfRows, String baseDate, String baseTime, String placeX, String placeY);
    ArrayList<WeatherDto> getWeather(int mode, LocationDto locationDto);
    String ApiTime();
    String ApiTimeChange(String time);
}
