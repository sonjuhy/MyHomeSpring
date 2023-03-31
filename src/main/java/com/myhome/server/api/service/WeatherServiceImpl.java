package com.myhome.server.api.service;

import com.google.gson.*;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class WeatherServiceImpl implements WeatherService{

    private String key;
    private String url_UltraNcst = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기실황
    private String url_UltraFcst = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst"; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기예보
    private String url_VilageFcst = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";// need key, pageNo, numOfRows, dataType, base_date, base_time, x, y | 동네예보조회
    private String url_main = null;

    private WeatherDto weather;
    private ArrayList<WeatherDto> weatherVilage, weatherUltra;

    @Override
    public String getKey() {
        String keyData = "8uiEDcNjEfxFOoq%2BIjRY2M7MAEKuW7AwNs9%2FyHFZUqmzm4Ci2hyvtfZdgZ7vGHBI6RjxsgBlnq%2BogcZfanSA%2Bw%3D%3D";
        return keyData;
    }

    @Override
    public String calVEC(String value) {
        double value_double = Double.parseDouble(value);
        int value_int = (int) Math.round(((value_double + 22.5*0.5)/22.5));
        String vec = null;
        switch(value_int){
            case 0:
            case 16:
                vec = "북";
                break;
            case 1:
            case 2:
            case 3:
                vec = "북동";
                break;
            case 4:
                vec = "동";
                break;
            case 5:
            case 6:
            case 7:
                vec = "남동";
                break;
            case 8:
                vec = "남";
                break;
            case 9:
            case 10:
            case 11:
                vec = "남서";
                break;
            case 12:
                vec = "서";
                break;
            case 13:
            case 14:
            case 15:
                vec = "북서";
                break;

        }
        return vec;
    }

    @Override
    public WeatherDto JsonParsing(JsonArray jsonArray, int mode) {
        JsonObject WeatherData;
        String DataValue = "";
        String info = "";
        String fsct_date = null;
        String fsct_time = null;
        int loop_max = 1;
        WeatherDto tmp_weather = new WeatherDto();
        ArrayList<WeatherDto> tmp_weathers = new ArrayList<>();
        for(int i=0;i<4;i++) tmp_weathers.add(new WeatherDto());
        System.out.println("tmp weather size : " + tmp_weathers.size());
        if(mode == 1){
            loop_max = 4;
        }
        try {
            for (int i = 0; i < jsonArray.size(); i++) {
                for(int j=0; j<loop_max; j++) {
                    WeatherData = (JsonObject) jsonArray.get(i);
                    info = WeatherData.get("category").toString();
                    System.out.println("info : " + info);
                    if (mode == 0) {
                        DataValue = WeatherData.get("obsrValue").toString();
                        System.out.println("info value : " + info);
                        System.out.println("Data value : " + DataValue);
                    } else {
                        DataValue = WeatherData.get("fcstValue").toString();
                        fsct_time = WeatherData.get("fcstTime").toString();
                        fsct_time = fsct_time.substring(0,2)+":00";
                        if("00:00".equals(fsct_time)){
                            fsct_time = "24:00";
                        }
                        fsct_date = WeatherData.get("fcstDate").toString();
                        tmp_weather.setFcstDate(fsct_date);
                        tmp_weather.setFcstTime(fsct_time);
                    }

                    switch (mode) {
                        case 0:// T1H, RN1, UUU, VVV, REH, PTY, VEC, WSD
                        {
                            if (info.equals("WSD")) {
                                info = "풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setWSD(DataValue);
                            }
                            if (info.equals("RN1")) {
                                info = "시간당강수량";
                                DataValue = DataValue;// + " mm";
                                tmp_weather.setRN1(DataValue);
                            }
                            if (info.equals("REH")) {
                                info = "습도";
                                DataValue = DataValue;// + "%";
                                tmp_weather.setREH(DataValue);
                            }
                            if (info.equals("UUU")) {
                                info = "동서성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setUUU(DataValue);
                            }
                            if (info.equals("VVV")) {
                                info = "남북성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setVVV(DataValue);
                            }
                            if (info.equals("T1H")) {
                                info = "기온";
                                DataValue = DataValue;// + "℃";
                                tmp_weather.setT1H(DataValue);
                            }
                            if (info.equals("PTY")) {
                                info = "강수형태";
                                if (DataValue.equals("0")) {
                                    DataValue = "없음";
                                } else if (DataValue.equals("1")) {
                                    DataValue = "비";
                                } else if (DataValue.equals("2")) {
                                    DataValue = "눈/비";
                                } else if (DataValue.equals("3")) {
                                    DataValue = "눈";
                                }
                                tmp_weather.setPTY(DataValue);
                            }
                            if (info.equals("VEC")) {
                                info = "풍향";
                                DataValue = DataValue;// + " m/s";
                                DataValue = calVEC(DataValue);
                                tmp_weather.setVEC(DataValue);
                            }
                            weather = tmp_weather;
                        }
                        break;
                        case 1: //T1H, RN1, SKY, UUU, VVV, REH, PTY, LGT, VEC, WSD
                        {
                            if (info.equals("LGT")) {
                                info = "낙뢰";
                                if (DataValue.equals("0")) {
                                    tmp_weather.setLGT("없음");
                                } else if (DataValue.equals("1")) {
                                    tmp_weather.setLGT("있음");
                                }
                            }
                            if (info.equals("WSD")) {
                                info = "풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setWSD(DataValue);
                            }
                            if (info.equals("RN1")) {
                                info = "시간당강수량";
                                DataValue = DataValue;// + " mm";
                                tmp_weather.setRN1(DataValue);
                            }
                            if (info.equals("REH")) {
                                info = "습도";
                                DataValue = DataValue;// + "%";
                                tmp_weather.setREH(DataValue);
                            }
                            if (info.equals("SKY")) {
                                info = "하늘상태";
                                if (DataValue.equals("1")) {
                                    DataValue = "맑음";
                                } else if (DataValue.equals("2")) {
                                    DataValue = "비";
                                } else if (DataValue.equals("3")) {
                                    DataValue = "구름많음";
                                } else if (DataValue.equals("4")) {
                                    DataValue = "흐림";
                                }
                                tmp_weather.setSKY(DataValue);
                            }
                            if (info.equals("UUU")) {
                                info = "동서성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setUUU(DataValue);
                            }
                            if (info.equals("VVV")) {
                                info = "남북성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setVVV(DataValue);
                            }
                            if (info.equals("T1H")) {
                                info = "기온";
                                DataValue = DataValue;// + "℃";
                                tmp_weather.setT1H(DataValue);
                            }
                            if (info.equals("PTY")) {
                                info = "강수형태";
                                if (DataValue.equals("0")) {
                                    DataValue = "없음";
                                } else if (DataValue.equals("1")) {
                                    DataValue = "비";
                                } else if (DataValue.equals("2")) {
                                    DataValue = "눈/비";
                                } else if (DataValue.equals("3")) {
                                    DataValue = "눈";
                                }
                                tmp_weather.setPTY(DataValue);
                            }
                            if (info.equals("VEC")) {
                                info = "풍향";
                                DataValue = DataValue;// + " m/s";
                                DataValue = calVEC(DataValue);
                                tmp_weather.setVEC(DataValue);
                            }
                            tmp_weathers.set(i, tmp_weather);
                            break;
                        }
                        case 2:// *POP, *PTY, *R06, *REH, *S06, *SKY, *T3H, *TMN, *TMX, *UUU, *VVV, *WAV, *VEC, *WSD
                        {
                            tmp_weather = new WeatherDto();
                            if (info.equals("POP")) {
                                info = "강수확률";
                                DataValue = DataValue;// + " %";
                                tmp_weather.setPOP(DataValue);
                            }
                            if (info.equals("REH")) {
                                info = "습도";
                                DataValue = DataValue;// + " %";
                                tmp_weather.setREH(DataValue);
                            }
                            if (info.equals("SKY")) {
                                info = "하늘상태";
                                if (DataValue.equals("1")) {
                                    DataValue = "맑음";
                                } else if (DataValue.equals("2")) {
                                    DataValue = "비";
                                } else if (DataValue.equals("3")) {
                                    DataValue = "구름많음";
                                } else if (DataValue.equals("4")) {
                                    DataValue = "흐림";
                                }
                                tmp_weather.setSKY(DataValue);
                            }
                            if (info.equals("UUU")) {
                                info = "동서성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setUUU(DataValue);
                            }
                            if (info.equals("VVV")) {
                                info = "남북성분풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setVVV(DataValue);
                            }
                            if (info.equals("R06")) {
                                info = "6시간강수량";
                                DataValue = DataValue;// + " mm";
                                tmp_weather.setR06(DataValue);
                            }
                            if (info.equals("S06")) {
                                info = "6시간적설량";
                                DataValue = DataValue;// + " mm";
                                tmp_weather.setS06(DataValue);
                            }
                            if (info.equals("PTY")) {
                                info = "강수형태";
                                if (DataValue.equals("0")) {
                                    DataValue = "없음";
                                } else if (DataValue.equals("1")) {
                                    DataValue = "비";
                                } else if (DataValue.equals("2")) {
                                    DataValue = "눈/비";
                                } else if (DataValue.equals("3")) {
                                    DataValue = "눈";
                                }
                                tmp_weather.setPTY(DataValue);
                            }
                            if (info.equals("T3H")) {
                                info = "3시간기온";
                                DataValue = DataValue;// + " ℃";
                                tmp_weather.setT3H(DataValue);
                            }
                            if (info.equals("VEC")) {
                                info = "풍향";
                                DataValue = DataValue;// + " m/s";
                                DataValue = calVEC(DataValue);
                                tmp_weather.setVEC(DataValue);
                            }
                                /*if (info.equals("WAV")) {
                                    info = "파고";
                                    DataValue = DataValue + " M";
                                    weather.setWSD(DataValue);
                                }*/
                            if(info.equals("TMN")) {
                                info = "아침최저기온";
                                //DataValue = DataValue + " ℃";
                                tmp_weather.setTMN(DataValue);
                            }
                            if(info.equals("TMX")) {
                                info = "낮최고기온";
                                //DataValue = DataValue + " ℃";
                                tmp_weather.setTMX(DataValue);
                            }
                            if (info.equals("WSD")) {
                                info = "풍속";
                                DataValue = DataValue;// + " m/s";
                                tmp_weather.setWSD(DataValue);
                            }
                            weatherVilage.add(tmp_weather);
                            break;
                        }
                    }
                }
            }
            if(mode == 1){
                for(int i=0;i<4;i++){
                    this.weatherUltra.add(tmp_weathers.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp_weather;
    }

    @Override
    public String fnJson(String Data, int mode) {
        try {
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(Data);
            JsonObject object = element.getAsJsonObject();
            JsonObject objectRes = object.getAsJsonObject("response");
            JsonObject objectHeader = objectRes.getAsJsonObject("header");
            JsonObject objectBody = objectRes.getAsJsonObject("body");
            JsonObject objectItem = objectBody.getAsJsonObject("items");
            JsonArray array = objectItem.getAsJsonArray("item");

            System.out.println("json array length : " + array.size());
            System.out.println("mode in fn_jsonp : " + mode);

            String result = objectHeader.get("resultCode").getAsString();

            if(!"00".equals(result)){
                System.out.println("JSON data is error : " + result);
                return result;
            }
            else{
                if(mode == 0) {
                    this.weather = JsonParsing(array, mode);
                }
                else {
                    JsonParsing(array, mode);
                }
            }
        } catch (Exception e) {
            System.out.println("error in fn_Jsnop : " + e.getMessage());
        }

        return "Success";
    }

    @Override
    public String getWeatherInfo(String value, String numOfRows, String baseDate, String baseTime, String placeX, String placeY) {
        URLConnection conn;
        int loop_max, mode;
        String pageNo;
        String ConnectValue = "";
        this.key = this.getKey();
        weather = new WeatherDto();

        if("UltraNcst".equals(value)){
            mode = 0;
            loop_max = 1;
            weatherUltra = new ArrayList<>();
        }else if("UltraFcst".equals(value)){
            mode = 1;
            loop_max = 4;
            weatherUltra = new ArrayList<>();
        }
        else if("VilageFcst".equals(value)){
            mode = 2;
            loop_max = 1;
            weatherVilage = new ArrayList<>();
        }
        else{
            mode = -1;
            loop_max = 0;
        }
        pageNo = Integer.toString(loop_max);

        switch(mode){
            case 0:
                url_main = url_UltraNcst + "?serviceKey=" + key + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=JSON&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + placeX + "&ny=" + placeY;
                break;
            case 1:
                url_main = url_UltraFcst + "?serviceKey=" + key + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=JSON&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + placeX + "&ny=" + placeY;
                break;
            case 2:
                url_main = url_VilageFcst + "?serviceKey=" + key + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=JSON&base_date=" + baseDate + "&base_time=" + baseTime + "&nx=" + placeX + "&ny=" + placeY;
                break;
            default:
                System.out.println("Weather class mode error");
                break;
        }
        try {
            URL url = new URL(url_main);
            System.out.println("url : " + url_main);
            conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            for(int i=0;i<loop_max;i++) {
                String ResData = br.readLine();

                if (ResData == null) {
                    System.out.println("응답데이터 == NULL");
                } else {
                    System.out.println("br ResData(" +i+") : "+ResData);
                    ConnectValue = fnJson(ResData, mode);
                    if(!"Success".equals(ConnectValue)){
                        System.out.println("JSON data is error : " + ConnectValue);
                        break;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.out.println("error : " + e.getMessage());
        }
        return ConnectValue;
    }

    @Override
    public ArrayList<WeatherDto> getWeather(int mode, LocationDto locationDto) {
        StringTokenizer st = new StringTokenizer(ApiTime());
        String date = st.nextToken();
        String time = ApiTimeChange(st.nextToken());
        String Xcode = Integer.toString(locationDto.getX_code()), Ycode = Integer.toString(locationDto.getY_code());
        String result = null;
        switch (mode){
            case 0: // get Ultra Ncst
                result = getWeatherInfo("UltraNcst", "10", date, time, Xcode, Ycode);
                if("Success".equals(result)) return null;
                break;
            case 1: // get Ultra Fcst
                result = getWeatherInfo("UltraFcst", "10", date, time, Xcode, Ycode);
                if("Success".equals(result)) return weatherUltra;
                break;
            case 2: // get Vilage Fcst
                result = getWeatherInfo("VilageFcst", "10", date, time, Xcode, Ycode);
                System.out.println("vilage fcst : " + result+", size : " + weatherVilage.size());
                if("Success".equals(result)) return weatherVilage;
                break;
        }
        return null;
    }

    @Override
    public String ApiTime() {
        SimpleDateFormat Format = new SimpleDateFormat("yyyyMMdd HHmmss");
        Date time = new Date();
        String timeStr = Format.format(time);
        return timeStr;
    }

    @Override
    public String ApiTimeChange(String time) {
        String hh = time.substring(0,2);
        String baseTime = "";
        hh = hh + "00";


        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트) //
        switch (hh) {

            case "0200":
            case "0300":
            case "0400":
                baseTime = "0200";
                break;
            case "0500":
            case "0600":
            case "0700":
                baseTime = "0500";
                break;
            case "0800":
            case "0900":
            case "1000":
                baseTime = "0800";
                break;
            case "1100":
            case "1200":
            case "1300":
                baseTime = "1100";
                break;
            case "1400":
            case "1500":
            case "1600":
                baseTime = "1400";
                break;
            case "1700":
            case "1800":
            case "1900":
                baseTime = "1700";
                break;
            case "2000":
            case "2100":
            case "2200":
                baseTime = "2000";
                break;
            default:
                baseTime = "2300";

        }
        return baseTime;
    }
}
