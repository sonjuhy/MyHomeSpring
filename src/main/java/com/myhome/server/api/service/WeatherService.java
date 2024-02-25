package com.myhome.server.api.service;

import com.google.gson.JsonArray;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.SGISDto.SGISAddressDto;
import com.myhome.server.api.dto.openWeatherDto.ForecastDayDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherCurrentDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherForecastDto;
import com.myhome.server.api.dto.WeatherDto;

import java.util.ArrayList;
import java.util.List;

public interface WeatherService {
    String getKey();
    String getLinkUltraNcst();
    String getLinkUltraFcst();
    String getLinkVilageFcst();
    String calVEC(String value);
    ArrayList<WeatherDto> JsonParsing(JsonArray jsonArray, int mode);
    JsonArray fnJson(String Data);
    WeatherDto getUtlraNcst(LocationDto locationDto); // 초단기실황조회
    ArrayList<WeatherDto> getUtlraFcst(LocationDto locationDto); // 초단기예보조회
    ArrayList<WeatherDto> getVilageFcst(LocationDto locationDto); // 단기예보조회
    ArrayList<LocationDto> getTopPlace();
    ArrayList<LocationDto> getMiddlePlace(String code);
    ArrayList<LocationDto> getLeafPlace(String code);
    String ApiTime();
    String ApiTimeChange(String time);

    String getSGISAccessToken();
    SGISAddressDto getSGISAddressInfo(int cd);
    double[] convertCoordinate(double x, double y);
    OpenWeatherCurrentDto getCurrentWeatherInfo(double lat, double lon);
    OpenWeatherForecastDto getForecastWeatherInfo(double lat, double lon);
    OpenWeatherCurrentDto getCurrentWeatherInfoByCoordinate(int x, int y);
    OpenWeatherForecastDto getForecastWeatherInfoByCoordinate(int x, int y);
    List<ForecastDayDto> get5DayAverageWeatherInfo(double lat, double lon);
    List<ForecastDayDto> get5DayAverageWeatherInfoByCoordinate(int x, int y);
}
