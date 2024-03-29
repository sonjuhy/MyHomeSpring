package com.myhome.server.api.service;

import com.google.gson.*;
import com.myhome.server.api.dto.LocationDto;
import com.myhome.server.api.dto.SGISDto.SGISAddressDto;
import com.myhome.server.api.dto.SGISDto.geoCodeDto.GeoCodeDto;
import com.myhome.server.api.dto.SGISDto.tokenDto.SGISTokenDto;
import com.myhome.server.api.dto.SGISDto.tokenDto.SGISTokenResultDto;
import com.myhome.server.api.dto.openWeatherDto.ForecastDayDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherCurrentDto;
import com.myhome.server.api.dto.openWeatherDto.OpenWeatherForecastDto;
import com.myhome.server.api.dto.WeatherDto;
import com.myhome.server.api.dto.openWeatherDto.forecast.OpenWeatherForecastItemDto;
import com.myhome.server.api.dto.openWeatherDto.forecast.OpenWeatherForecastItemMainDto;
import com.myhome.server.api.dto.openWeatherDto.forecast.OpenWeatherForecastItemWeatherDto;
import com.myhome.server.component.KafkaProducer;
import com.myhome.server.component.LogComponent;
import com.myhome.server.db.entity.WeatherAPIKeyEntity;
import com.myhome.server.db.entity.WeatherKeyEntity;
import com.myhome.server.db.repository.WeatherAPIKeyRepository;
import com.myhome.server.db.repository.WeatherAPIRepository;
import com.myhome.server.db.repository.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeatherServiceImpl implements WeatherService{

    private String key;
    private String url_UltraNcst = ""; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기실황
    private String url_UltraFcst = ""; //need key, pageNo, numOfRows, base_date, base_time, x, y | 초단기예보
    private String url_VilageFcst = "";// need key, pageNo, numOfRows, dataType, base_date, base_time, x, y | 동네예보조회
    private String url_main = null;
    private String SGIS_SECURITY_KEY;
    private String SGIS_SERVICE_KEY;
    private String OPENWEATHERAPI_KEY;
    private HashMap<Integer, String> openWeatherAPIWeatherHash;

    private final String TOPIC_WEATHER_LOG = "weather-log-topic";

    private final static String locationURL = "https://www.kma.go.kr/DFSROOT/POINT/DATA";

    @Autowired
    LogComponent logComponent;

    @Autowired
    WeatherRepository repository;
    @Autowired
    WeatherAPIRepository weatherAPIRepository;
    @Autowired
    WeatherAPIKeyRepository weatherAPIKeyRepository;

    @Autowired
    KafkaProducer producer;

    @Autowired
    public WeatherServiceImpl(WeatherAPIKeyRepository weatherAPIKeyRepository){
        OPENWEATHERAPI_KEY = weatherAPIKeyRepository.findByServiceName("OpenWeatherAPI").getKey();
        SGIS_SERVICE_KEY = weatherAPIKeyRepository.findByServiceName("SGISServiceKey").getKey();
        SGIS_SECURITY_KEY = weatherAPIKeyRepository.findByServiceName("SGISSecurityKey").getKey();

        // Thunderstorm, Drizzle, Rain, Snow, Atmosphere, Clear, Clouds
        openWeatherAPIWeatherHash = new HashMap<>();
        openWeatherAPIWeatherHash.put(0, "Clear");
        openWeatherAPIWeatherHash.put(1, "Clouds");
        openWeatherAPIWeatherHash.put(2, "Atmosphere");
        openWeatherAPIWeatherHash.put(3, "Rain");
        openWeatherAPIWeatherHash.put(4, "Snow");
        openWeatherAPIWeatherHash.put(5, "Drizzle");
        openWeatherAPIWeatherHash.put(6, "Thunderstorm");

        System.out.println("open api : "+OPENWEATHERAPI_KEY);
        System.out.println("service key : "+SGIS_SERVICE_KEY);
        System.out.println("security key : "+SGIS_SECURITY_KEY);
    }

    @Override
    public String getKey() {
        WeatherKeyEntity entity = repository.findByWeatherPk(1);
        String keyData = entity.getKey();
        return keyData;
    }

    @Override
    public String getLinkUltraNcst() {
        WeatherKeyEntity entity = repository.findByWeatherPk(1);
        String linkData = entity.getUltraNcst();
//        String linkData = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        return linkData;
    }

    @Override
    public String getLinkUltraFcst() {
        WeatherKeyEntity entity = repository.findByWeatherPk(1);
        String linkData = entity.getUltraFcst();
//        String linkData = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
        return linkData;
    }

    @Override
    public String getLinkVilageFcst() {
        WeatherKeyEntity entity = repository.findByWeatherPk(1);
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
                logComponent.sendLog("Weather", "[JsonParsing] tmp weather : "+tmp_weather+", mode : " + mode, true, TOPIC_WEATHER_LOG);
            }
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[JsonParsing] error : ", e, TOPIC_WEATHER_LOG);
            return null;
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

            String result = objectHeader.get("resultCode").getAsString();

            if(!"00".equals(result)){
                logComponent.sendLog("Weather", "[fnJson] JSON data is error : "+result, false, TOPIC_WEATHER_LOG);
                return null;
            }
            else{
                logComponent.sendLog("Weather", "[fnJson] array size : "+array.size(), true, TOPIC_WEATHER_LOG);
                return array;
            }
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[fnJson] error : ", e, TOPIC_WEATHER_LOG);
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
            conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String ResData = br.readLine();


            if (ResData == null) {
                logComponent.sendLog("Weather", "[getUtlraNcst] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
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
                logComponent.sendLog("Weather", "[getUtlraNcst] array size : "+array.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getUtlraNcst] getUltraNcst error : ", e, TOPIC_WEATHER_LOG);
            return null;
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
                logComponent.sendLog("Weather", "[getUtlraFsct] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
            } else {
                JsonArray array = fnJson(ResData);
                ArrayList<WeatherDto> list = JsonParsing(array, 1);
                logComponent.sendLog("Weather", "[getUtlraFsct] list size : "+list.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
                return list;
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getUtlraFsct] getUtlraFsct error : ", e, TOPIC_WEATHER_LOG);
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
                logComponent.sendLog("Weather", "[getVilageFcst] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
            } else {
                JsonArray array = fnJson(ResData);
                if(array == null) return null;
                ArrayList<WeatherDto> list = JsonParsing(array, 2);
                logComponent.sendLog("Weather", "[getVilageFcst] list size : "+list.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
                return list;
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getVilageFcst] getVilageFcst error : ", e, TOPIC_WEATHER_LOG);
        }

        return null;
    }

    @Override
    public ArrayList<LocationDto> getTopPlace() {
        ArrayList<LocationDto> list = new ArrayList<>();

        url_main = locationURL+"/top.json.txt";

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
                logComponent.sendLog("Weather", "[getTopPlace] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
            } else {
                JsonArray jsonArray = (JsonArray) JsonParser.parseString(ResData);
                for(int i=0;i<jsonArray.size();i++){
                    JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                    LocationDto dto = new LocationDto();
                    dto.setName(jsonObject.get("value").getAsString());
                    dto.setCode(jsonObject.get("code").getAsString());
                    list.add(dto);
                }
                logComponent.sendLog("Weather", "[getTopPlace] list size : "+list.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
                return list;
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getTopPlace] getTopPlace error : ", e, TOPIC_WEATHER_LOG);
        }
        return null;
    }

    @Override
    public ArrayList<LocationDto> getMiddlePlace(String code) {
        ArrayList<LocationDto> list = new ArrayList<>();

        url_main = locationURL+"/mdl."+code+".json.txt";

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
                logComponent.sendLog("Weather", "[getMiddlePlace] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
            } else {
                JsonArray jsonArray = (JsonArray) JsonParser.parseString(ResData);
                for(int i=0;i<jsonArray.size();i++){
                    JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                    LocationDto dto = new LocationDto();
                    dto.setName(jsonObject.get("value").getAsString());
                    dto.setCode(jsonObject.get("code").getAsString());
                    list.add(dto);
                }
                logComponent.sendLog("Weather", "[getMiddlePlace] list size : "+list.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
                return list;
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getMiddlePlace] getMiddlePlace error : ", e, TOPIC_WEATHER_LOG);
        }
        return null;
    }

    @Override
    public ArrayList<LocationDto> getLeafPlace(String code) {
        ArrayList<LocationDto> list = new ArrayList<>();

        url_main = locationURL+"/leaf."+code+".json.txt";

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
                logComponent.sendLog("Weather", "[getLeafPlace] 응답데이터 == NULL", false, TOPIC_WEATHER_LOG);
            } else {
                JsonArray jsonArray = (JsonArray) JsonParser.parseString(ResData);
                for(int i=0;i<jsonArray.size();i++){
                    JsonObject jsonObject = (JsonObject) jsonArray.get(i);
                    LocationDto dto = new LocationDto();
                    dto.setX_code(jsonObject.get("x").getAsInt());
                    dto.setY_code(jsonObject.get("y").getAsInt());
                    dto.setName(jsonObject.get("value").getAsString());
                    dto.setCode(jsonObject.get("code").getAsString());
                    list.add(dto);
                }
                logComponent.sendLog("Weather", "[getLeafPlace] list size : "+list.size()+", url : "+url_main, true, TOPIC_WEATHER_LOG);
                return list;
            }
            br.close();
        } catch (Exception e) {
            logComponent.sendErrorLog("Weather", "[getLeafPlace] getLeafPlace error : ", e, TOPIC_WEATHER_LOG);
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

    @Override
    public String getSGISAccessToken() {
        WebClient webClient = WebClient.builder().baseUrl("https://sgisapi.kostat.go.kr/OpenAPI3").build();

        SGISTokenDto token = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/authentication.json")
                        .queryParam("consumer_key", SGIS_SERVICE_KEY)
                        .queryParam("consumer_secret", SGIS_SECURITY_KEY)
                        .build()
                )
                .retrieve()
                .bodyToMono(SGISTokenDto.class)
                .block();
        if(token.getErrCd() == 0) return token.getResult().getAccessToken();
        else return "error";
    }

    @Override
    public SGISAddressDto getSGISAddressInfo(int cd) {
        String key = getSGISAccessToken();
        if("error".equals(key)) return null;
        WebClient webClient = WebClient.builder().baseUrl("https://sgisapi.kostat.go.kr/OpenAPI3").build();
        SGISAddressDto addressDto;
        if(cd == 0){
            addressDto = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/addr/stage.json")
                            .queryParam("accessToken", key)
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(SGISAddressDto.class)
                    .block();
        }
        else{
            addressDto = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/addr/stage.json")
                            .queryParam("accessToken", key)
                            .queryParam("cd", String.valueOf(cd))
                            .build()
                    )
                    .retrieve()
                    .bodyToMono(SGISAddressDto.class)
                    .block();
        }
        return addressDto;
    }

    @Override
    public double[] convertCoordinate(double x, double y) {
        String key = getSGISAccessToken();
        if("error".equals(key)) return null;
        WebClient webClient = WebClient.builder().baseUrl("https://sgisapi.kostat.go.kr/OpenAPI3").build();
        GeoCodeDto geoCodeDto = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transformation/transcoord.json")
                        .queryParam("accessToken", key)
                        .queryParam("src","5179")
                        .queryParam("dst","4326")
                        .queryParam("posX", x)
                        .queryParam("posY", y)
                        .build()
                )
                .retrieve()
                .bodyToMono(GeoCodeDto.class)
                .block();
        if(geoCodeDto != null && geoCodeDto.getErrCd() == 0) {
            return new double[]{geoCodeDto.getResult().getPosX(), geoCodeDto.getResult().getPosY()};
        }
        else{
            return new double[]{};
        }
    }

    @Override
    public OpenWeatherCurrentDto getCurrentWeatherInfo(double lat, double lon) {
        WebClient webClient = WebClient.builder().baseUrl("https://api.openweathermap.org/data/2.5/").build();
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("lon", lon)
                        .queryParam("lat", lat)
                        .queryParam("appid", OPENWEATHERAPI_KEY)
                        .build()
                )
                .retrieve()
                .bodyToMono(OpenWeatherCurrentDto.class)
                .map(response -> {
                    response.getMain().setTempMin(
                            (float) ((float) Math.round((response.getMain().getTempMin() - 273.15)*100)/100.0)
                    );
                    response.getMain().setTempMax(
                            (float) ((float) Math.round((response.getMain().getTempMax() - 273.15)*100)/100.0)
                    );
                    response.getMain().setTemp(
                            (float) ((float) Math.round((response.getMain().getTemp() - 273.15)*100)/100.0)
                    );
                    response.getMain().setFeelsLike(
                            (float) ((float) Math.round((response.getMain().getFeelsLike() - 273.15)*100)/100.0)
                    );
                    return response;
                })
                .block();
    }

    @Override
    public OpenWeatherForecastDto getForecastWeatherInfo(double lat, double lon) {
        WebClient webClient = WebClient.builder().baseUrl("https://api.openweathermap.org/data/2.5/").build();
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("lon", lon)
                        .queryParam("lat", lat)
                        .queryParam("appid", OPENWEATHERAPI_KEY)
                        .build()
                )
                .retrieve()
                .bodyToMono(OpenWeatherForecastDto.class)
                .map(response -> {
                    for(OpenWeatherForecastItemDto dto : response.getList()){
                        dto.getMain().setTempMin(
                                (float) ((float) Math.round((dto.getMain().getTempMin() - 273.15)*100)/100.0)
                        );
                        dto.getMain().setTempMax(
                                (float) ((float) Math.round((dto.getMain().getTempMax() - 273.15)*100)/100.0)
                        );
                        dto.getMain().setTemp(
                                (float) ((float) Math.round((dto.getMain().getTemp() - 273.15)*100)/100.0)
                        );
                        dto.getMain().setFeelsLike(
                                (float) ((float) Math.round((dto.getMain().getFeelsLike() - 273.15)*100)/100.0)
                        );
                    }
                    return response;
                })
                .block();
    }

    @Override
    public OpenWeatherCurrentDto getCurrentWeatherInfoByCoordinate(int x, int y) {
        double[] lonLat = convertCoordinate(x, y);
        if(lonLat == null) return null;
        return getCurrentWeatherInfo(lonLat[1], lonLat[0]);
    }

    @Override
    public OpenWeatherForecastDto getForecastWeatherInfoByCoordinate(int x, int y) {
        double[] lonLat = convertCoordinate(x, y);
        if(lonLat == null) return null;
        return getForecastWeatherInfo(lonLat[1], lonLat[0]);
    }

    @Override
    public List<ForecastDayDto> get5DayAverageWeatherInfo(double lat, double lon) {
        List<ForecastDayDto> list = new ArrayList<>();
        List<OpenWeatherForecastItemDto> itemList = getForecastWeatherInfo(lat, lon).getList();
        float min = 1000, max = 0;
        int weatherMax = 0, point = 0;

        int[] weatherCount = new int[7];
        for(int i=0;i<40;i++){
            OpenWeatherForecastItemWeatherDto weatherDto = itemList.get(i).getWeather().get(0);
            OpenWeatherForecastItemMainDto weatherMainDto = itemList.get(i).getMain();
            switch(weatherDto.getMain()){
                case "Clear":
                    weatherCount[0]++;
                    break;
                case "Clouds":
                    weatherCount[1]++;
                    break;
                case "Atmosphere":
                    weatherCount[2]++;
                    break;
                case "Rain":
                    weatherCount[3]++;
                    break;
                case "Snow":
                    weatherCount[4]++;
                    break;
                case "Drizzle":
                    weatherCount[5]++;
                    break;
                case "ThunderStorm":
                    weatherCount[6]++;
                    break;
            }
            min = Math.min(min, weatherMainDto.getTempMin());
            max = Math.max(max, weatherMainDto.getTempMax());
            if((i + 1) % 8 == 0){
                for(int c=0;c<7;c++){
                    int count = weatherCount[c];
                    if(count > weatherMax){
                        weatherMax = count;
                        point = c;
                    }
                }
                int dt = itemList.get(i).getDt();
                Instant instant = Instant.ofEpochSecond(dt);
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));

                DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
                String dayOfWeekString = switch (dayOfWeek.name()) {
                    case "MONDAY" -> "월";
                    case "TUESDAY" -> "화";
                    case "WEDNESDAY" -> "수";
                    case "THURSDAY" -> "목";
                    case "FRIDAY" -> "금";
                    case "SATURDAY" -> "토";
                    case "SUNDAY" -> "일";
                    default -> "";
                };
                String monthStr = localDateTime.getMonthValue() < 10 ? "0"+localDateTime.getMonthValue() : String.valueOf(localDateTime.getMonthValue());
                String dayStr = localDateTime.getDayOfMonth() < 10 ? "0"+localDateTime.getDayOfMonth() : String.valueOf(localDateTime.getDayOfMonth());

                ForecastDayDto dayDto = new ForecastDayDto(
                        openWeatherAPIWeatherHash.get(point),
                        dayOfWeekString,
                        monthStr + dayStr,
                        Math.round(min*100)/100.0,
                        Math.round(max*100)/100.0
                );
                list.add(dayDto);
                min = 1000;
                max = 0;
                Arrays.fill(weatherCount, 0);
            }
        }
        return list;
    }

    @Override
    public List<ForecastDayDto> get5DayAverageWeatherInfoByCoordinate(int x, int y) {
        List<ForecastDayDto> list;
        double[] lonLat = convertCoordinate(x, y);
        if(lonLat == null) return null;
        list = get5DayAverageWeatherInfo(lonLat[1], lonLat[1]);
//        List<OpenWeatherForecastItemDto> itemList = getForecastWeatherInfoByCoordinate(x, y).getList();
//        float min = 1000, max = 0;
//        int weatherMax = 0, point = 0;
//
//        int[] weatherCount = new int[7];
//        for(int i=0;i<40;i++){
//            OpenWeatherForecastItemWeatherDto weatherDto = itemList.get(i).getWeather().get(0);
//            OpenWeatherForecastItemMainDto weatherMainDto = itemList.get(i).getMain();
//            switch(weatherDto.getMain()){
//                case "Clear":
//                    weatherCount[0]++;
//                    break;
//                case "Clouds":
//                    weatherCount[1]++;
//                    break;
//                case "Atmosphere":
//                    weatherCount[2]++;
//                    break;
//                case "Rain":
//                    weatherCount[3]++;
//                    break;
//                case "Snow":
//                    weatherCount[4]++;
//                    break;
//                case "Drizzle":
//                    weatherCount[5]++;
//                    break;
//                case "ThunderStorm":
//                    weatherCount[6]++;
//                    break;
//            }
//            min = Math.min(min, weatherMainDto.getTempMin());
//            max = Math.max(max, weatherMainDto.getTempMax());
//            if((i + 1) % 8 == 0){
//                for(int c=0;c<7;c++){
//                    int count = weatherCount[c];
//                    if(count > weatherMax){
//                        weatherMax = count;
//                        point = c;
//                    }
//                }
//                int dt = itemList.get(i).getDt();
//                Instant instant = Instant.ofEpochSecond(dt);
//                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
//
//                DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
//                String dayOfWeekString = switch (dayOfWeek.name()) {
//                    case "MONDAY" -> "월";
//                    case "TUESDAY" -> "화";
//                    case "WEDNESDAY" -> "수";
//                    case "THURSDAY" -> "목";
//                    case "FRIDAY" -> "금";
//                    case "SATURDAY" -> "토";
//                    case "SUNDAY" -> "일";
//                    default -> "";
//                };
//                String monthStr = localDateTime.getMonthValue() < 10 ? "0"+localDateTime.getMonthValue() : String.valueOf(localDateTime.getMonthValue());
//                String dayStr = localDateTime.getDayOfMonth() < 10 ? "0"+localDateTime.getDayOfMonth() : String.valueOf(localDateTime.getDayOfMonth());
//
//                int time = Integer.parseInt(monthStr + dayStr);
//                ForecastDayDto dayDto = new ForecastDayDto(
//                        openWeatherAPIWeatherHash.get(point),
//                        dayOfWeekString,
//                        time,
//                        Math.round(min*100)/100.0,
//                        Math.round(max*100)/100.0
//                );
//                list.add(dayDto);
//                min = 1000;
//                max = 0;
//                Arrays.fill(weatherCount, 0);
//            }
//        }
        return list;
    }
}

