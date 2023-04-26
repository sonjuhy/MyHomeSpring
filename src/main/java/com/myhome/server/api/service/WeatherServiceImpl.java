package com.myhome.server.api.service;

import com.google.gson.*;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.WeatherDto;
import com.myhome.server.db.entity.WeatherKeyEntity;
import com.myhome.server.db.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.StringTokenizer;

@Service
public class WeatherServiceImpl implements WeatherService{

    private String key;
    private String url_UltraNcst = ""; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기실황
    private String url_UltraFcst = ""; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기예보
    private String url_VilageFcst = "";// need key, pageNo, numOfRows, dataType, base_date, base_time, x, y | 동네예보조회
    private String url_main = null;

    @Autowired
    WeatherRepository repository;

    @Override
    public String getKey() {
        WeatherKeyEntity entity = repository.findByIdWeatherApi(1);
        String keyData = entity.getKey();
        return keyData;
    }

    @Override
    public String getLinkUltraNcst() {
        WeatherKeyEntity entity = repository.findByIdWeatherApi(1);
        String linkData = entity.getUltraNcst();
//        String linkData = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        return linkData;
    }

    @Override
    public String getLinkUltraFcst() {
        WeatherKeyEntity entity = repository.findByIdWeatherApi(1);
        String linkData = entity.getUltraFcst();
//        String linkData = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
        return linkData;
    }

    @Override
    public String getLinkVilageFcst() {
        WeatherKeyEntity entity = repository.findByIdWeatherApi(1);
        String linkData = entity.getVilageFcst();
//        String linkData = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        return linkData;
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
    public ArrayList<WeatherDto> JsonParsing(JsonArray jsonArray, int mode) {
        JsonObject WeatherData;
        String DataValue = "";
        String info = "";
        String fsct_date = null;
        String fsct_time = null;

        ArrayList<WeatherDto> tmp_weathers = new ArrayList<>();

        try {
            WeatherDto tmp_weather = null;
            String tmpTime = "";
            for (int i = 0; i < jsonArray.size(); i++) {

                WeatherData = (JsonObject) jsonArray.get(i);
                info = WeatherData.get("category").getAsString();

                DataValue = WeatherData.get("fcstValue").getAsString();
                fsct_time = WeatherData.get("fcstTime").getAsString();
                fsct_time = fsct_time.substring(0,2)+":00";
                if("00:00".equals(fsct_time)){
                    fsct_time = "24:00";
                }

                fsct_date = WeatherData.get("fcstDate").getAsString();

                switch (mode) {
                    case 1: //T1H, RN1, SKY, UUU, VVV, REH, PTY, LGT, VEC, WSD
                    {
                        tmp_weather = new WeatherDto();
                        tmp_weather.setFcstDate(fsct_date);
                        tmp_weather.setFcstTime(fsct_time);

                        if (info.equals("LGT")) {
                            info = "낙뢰";
                            tmp_weather.setType("LGT");
                            if (DataValue.equals("0")) {
                                tmp_weather.setLGT("없음");
                            } else if (DataValue.equals("1")) {
                                tmp_weather.setLGT("있음");
                            }
                        }
                        if (info.equals("WSD")) {
                            info = "풍속";
                            tmp_weather.setType("WSD");
                            tmp_weather.setWSD(DataValue);
                        }
                        if (info.equals("RN1")) {
                            info = "시간당강수량";
                            tmp_weather.setType("RN1");
                            tmp_weather.setRN1(DataValue);
                        }
                        if (info.equals("REH")) {
                            info = "습도";
                            tmp_weather.setType("REH");
                            tmp_weather.setREH(DataValue);
                        }
                        if (info.equals("SKY")) {
                            info = "하늘상태";
                            tmp_weather.setType("SKY");
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
                            tmp_weather.setType("UUU");
                            tmp_weather.setUUU(DataValue);
                        }
                        if (info.equals("VVV")) {
                            info = "남북성분풍속";
                            tmp_weather.setType("VVV");
                            tmp_weather.setVVV(DataValue);
                        }
                        if (info.equals("T1H")) {
                            info = "기온";
                            tmp_weather.setType("T1H");
                            tmp_weather.setT1H(DataValue);
                        }
                        if (info.equals("PTY")) {
                            info = "강수형태";
                            tmp_weather.setType("PTY");
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
                            tmp_weather.setType("VEC");
                            DataValue = calVEC(DataValue);
                            tmp_weather.setVEC(DataValue);
                        }
                        tmp_weathers.add(tmp_weather);
                        break;
                    }
                    case 2:// *POP, *PTY, *R06, *REH, *S06, *SKY, *TMN, *TMX, *UUU, *VVV, *WAV, *VEC, *WSD
                    {
                        if(!tmpTime.equals(fsct_time)) {
                            if(tmp_weather != null) tmp_weathers.add(tmp_weather);
                            tmp_weather = new WeatherDto();
                        }
                        tmpTime = fsct_time;

                        tmp_weather.setFcstDate(fsct_date);
                        tmp_weather.setFcstTime(fsct_time);

                        if (info.equals("PCP")) {
                            info = "1시간 강수량";
                            tmp_weather.setType("PCP");
                            tmp_weather.setPCP(DataValue);
                        }
                        if (info.equals("SNO")) {
                            info = "1시간 신적설";
                            tmp_weather.setType("SNO");
                            tmp_weather.setSNO(DataValue);
                        }
                        if (info.equals("TMP")) {
                            info = "1시간 기온";
                            tmp_weather.setType("TMP");
                            tmp_weather.setTMP(DataValue);
                        }
                        if (info.equals("POP")) {
                            info = "강수확률";
                            tmp_weather.setType("POP");
                            tmp_weather.setPOP(DataValue);
                        }
                        if (info.equals("REH")) {
                            info = "습도";
                            tmp_weather.setType("REH");
                            tmp_weather.setREH(DataValue);
                        }
                        if (info.equals("SKY")) {
                            info = "하늘상태";
                            tmp_weather.setType("SKY");
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
                            tmp_weather.setType("UUU");
                            tmp_weather.setUUU(DataValue);
                        }
                        if (info.equals("VVV")) {
                            info = "남북성분풍속";
                            tmp_weather.setType("VVV");
                            tmp_weather.setVVV(DataValue);
                        }
                        if (info.equals("PTY")) {
                            info = "강수형태";
                            tmp_weather.setType("PTY");
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
                            tmp_weather.setType("VEC");
                            DataValue = calVEC(DataValue);
                            tmp_weather.setVEC(DataValue);
                        }
                        if (info.equals("WAV")) {
                            info = "파고";
                            tmp_weather.setType("WAV");
                            tmp_weather.setWAV(DataValue);
                        }
                        if(info.equals("TMN")) {
                            info = "아침최저기온";
                            tmp_weather.setType("TMN");
                            tmp_weather.setTMN(DataValue);
                        }
                        if(info.equals("TMX")) {
                            info = "낮최고기온";
                            tmp_weather.setType("TMX");
                            tmp_weather.setTMX(DataValue);
                        }
                        if (info.equals("WSD")) {
                            info = "풍속";
                            tmp_weather.setType("WSD");
                            tmp_weather.setWSD(DataValue);
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tmp_weathers;
    }

    @Override
    public JsonArray fnJson(String Data) {
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

            String result = objectHeader.get("resultCode").getAsString();

            if(!"00".equals(result)){
                System.out.println("JSON data is error : " + result);
                return null;
            }
            else{
                return array;
            }
        } catch (Exception e) {
            System.out.println("error in fn_Jsnop : " + e.getMessage());
            return null;
        }
    }

    @Override
    public WeatherDto getUtlraNcst(LocationDto locationDto) {

        StringTokenizer st = new StringTokenizer(ApiTime());
        String date = st.nextToken();
        String timeBefore = st.nextToken();
        int mm = Integer.parseInt(timeBefore.substring(2,4));

        String time = "";
        if(mm < 30) {
            int timeInt = Integer.parseInt(timeBefore.substring(0,2)) - 1;
            if(timeInt < 10) time = "0"+timeInt+"00";
            else time = timeInt+"00";
        }
        else{
            time = timeBefore.substring(0,2)+"00";
        }
        String Xcode = Integer.toString(locationDto.getX_code()), Ycode = Integer.toString(locationDto.getY_code());
        String numOfRows = "10";

        this.key = getKey();
        this.url_UltraNcst = getLinkUltraNcst();

        URLConnection conn;
        String pageNo = "1";

        WeatherDto dto = new WeatherDto();

        url_main = url_UltraNcst + "?serviceKey=" + key + "&pageNo=" + pageNo + "&numOfRows=" + numOfRows + "&dataType=JSON&base_date=" + date + "&base_time=" + time + "&nx=" + Xcode + "&ny=" + Ycode;

        try {
            URL url = new URL(url_main);
            System.out.println("url : " + url_main);
            conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String ResData = br.readLine();


            if (ResData == null) {
                System.out.println("응답데이터 == NULL");
            } else {
                JsonArray array = fnJson(ResData);

                JsonObject WeatherData;
                String DataValue = "";
                String info = "";

                for (int i = 0; i < array.size(); i++) {
                    WeatherData = (JsonObject) array.get(i);
                    info = WeatherData.get("category").getAsString();
                    DataValue = WeatherData.get("obsrValue").getAsString();

                    if (info.equals("WSD")) {
                        info = "풍속";
                        dto.setType("WSD");
                        dto.setWSD(DataValue);
                    }
                    if (info.equals("RN1")) {
                        info = "시간당강수량";
                        dto.setType("RN1");
                        dto.setRN1(DataValue);
                    }
                    if (info.equals("REH")) {
                        info = "습도";
                        dto.setType("REH");
                        dto.setREH(DataValue);
                    }
                    if (info.equals("UUU")) {
                        info = "동서성분풍속";
                        dto.setType("UUU");
                        dto.setUUU(DataValue);
                    }
                    if (info.equals("VVV")) {
                        info = "남북성분풍속";
                        dto.setType("VVV");
                        dto.setVVV(DataValue);
                    }
                    if (info.equals("T1H")) {
                        info = "기온";
                        dto.setType("T1H");
                        dto.setT1H(DataValue);
                    }
                    if (info.equals("PTY")) {
                        info = "강수형태";
                        dto.setType("PTY");
                        switch (DataValue) {
                            case "0":
                                DataValue = "없음";
                                break;
                            case "1":
                                DataValue = "비";
                                break;
                            case "2":
                                DataValue = "눈/비";
                                break;
                            case "3":
                                DataValue = "눈";
                                break;
                        }
                        dto.setPTY(DataValue);
                    }
                    if (info.equals("VEC")) {
                        info = "풍향";
                        dto.setType("VEC");
                        DataValue = calVEC(DataValue);
                        dto.setVEC(DataValue);
                    }
                }

            }
            br.close();
        } catch (Exception e) {
            System.out.println("getUltraNcst error : " + e.getMessage());
        }
        return dto;
    }

    @Override
    public ArrayList<WeatherDto> getUtlraFcst(LocationDto locationDto) {

        this.key = getKey();
        this.url_UltraFcst = getLinkUltraFcst();

        StringTokenizer st = new StringTokenizer(ApiTime());
        String date = st.nextToken();
        String timeBefore = st.nextToken();
        int mm = Integer.parseInt(timeBefore.substring(2,4));

        String time = "";
        if(mm < 30) {
            int timeInt = Integer.parseInt(timeBefore.substring(0,2)) - 1;
            if(timeInt < 10) time = "0"+timeInt+"00";
            else time = timeInt+"00";
        }
        else{
            time = timeBefore.substring(0,2)+"00";
        }

        String Xcode = Integer.toString(locationDto.getX_code()), Ycode = Integer.toString(locationDto.getY_code());
        url_main = url_UltraFcst + "?serviceKey=" + key + "&pageNo=" + "1" + "&numOfRows=" + "10" + "&dataType=JSON&base_date=" + date + "&base_time=" + time + "&nx=" + Xcode + "&ny=" + Ycode;
        URLConnection conn;

        try {
            URL url = new URL(url_main);
            System.out.println("url : " + url_main);
            conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            String ResData = br.readLine();

            if (ResData == null) {
                System.out.println("응답데이터 == NULL");
            } else {
                JsonArray array = fnJson(ResData);
                ArrayList<WeatherDto> list = JsonParsing(array, 1);

                return list;
            }
            br.close();
        } catch (Exception e) {
            System.out.println("getUtlraFsct error : " + e.getMessage());
        }

        return null;
    }

    @Override
    public ArrayList<WeatherDto> getVilageFcst(LocationDto locationDto) {

        this.key = getKey();
        this.url_VilageFcst = getLinkVilageFcst();

        StringTokenizer st = new StringTokenizer(ApiTime());
        String date = st.nextToken();
        String time = ApiTimeChange(st.nextToken());
        if(Objects.equals(time, "2300")){
            int dateInt = Integer.parseInt(date);
            date = String.valueOf(dateInt-1);
        }
        String Xcode = Integer.toString(locationDto.getX_code()), Ycode = Integer.toString(locationDto.getY_code());

        url_main = url_VilageFcst + "?serviceKey=" + key + "&pageNo=" + "1" + "&numOfRows=" + "216" + "&dataType=JSON&base_date=" + date + "&base_time=" + time + "&nx=" + Xcode + "&ny=" + Ycode;

        URLConnection conn;

        try {
            URL url = new URL(url_main);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept","*/*;q=0.9");
            System.out.println("url : " + url_main);
            conn = url.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            String ResData = br.readLine();

            if (ResData == null) {
                System.out.println("응답데이터 == NULL");
            } else {
                JsonArray array = fnJson(ResData);
                if(array == null) return null;
                ArrayList<WeatherDto> list = JsonParsing(array, 2);

                return list;
            }
            br.close();
        } catch (Exception e) {
            System.out.println("getVilageFsct error : " + e.getMessage());
        }

        return null;
    }

    @Override
    public String ApiTime() {
        SimpleDateFormat Format = new SimpleDateFormat("yyyyMMdd HHmmss");
        Date time = new Date();
        return Format.format(time);
    }

    @Override
    public String ApiTimeChange(String time) {
        System.out.println("time : " + time);
        String hh = time.substring(0,2);
        int mm = Integer.parseInt(time.substring(2,4));
        if(mm < 10){
            int hhInt = Integer.parseInt(hh) - 1;
            if(hhInt < 10){
                hh = "0"+hhInt;
            }
            else hh = String.valueOf(hhInt);
        }
        String baseTime = "";

        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트) //
        switch (hh) {
            case "02":
            case "03":
            case "04":
                baseTime = "0200";
                break;
            case "05":
            case "06":
            case "07":
                baseTime = "0500";
                break;
            case "08":
            case "09":
            case "10":
                baseTime = "0800";
                break;
            case "11":
            case "12":
            case "13":
                baseTime = "1100";
                break;
            case "14":
            case "15":
            case "16":
                baseTime = "1400";
                break;
            case "17":
            case "18":
            case "19":
                baseTime = "1700";
                break;
            case "20":
            case "21":
            case "22":
                baseTime = "2000";
                break;
            default:
                baseTime = "2300";

        }
        return baseTime;
    }
}
