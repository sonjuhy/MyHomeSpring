package com.myhome.server.api.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class WeatherDto {
    private String Type;
    private String POP; //강수확률,%
    private String PTY; //강수형태, 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4), 빗방울(5), 빗방울/눈날림(6), 눈날림(7)
    private String REH; //습도 %
    private String SKY; //하늘상태, 맑음(1), 비(2), 구름많음(3), 흐림(4)
    private String T3H; //3시간 기온, ℃
    private String TMN; //아침 최저기온, ℃
    private String TMX; //낮 최고기온 ,℃
    private String UUU; //풍속(동서풍), m/s. + : 동, - : 서
    private String VVV; //풍속(남북풍), m/s, + : 북, - : 남
    private String WAV; //파고
    private String VEC; //풍향
    private String WSD; //풍속
    private String T1H; //기온, ℃
    private String RN1; //1시간 강수량, 1mm
    private String LGT; //낙뢰, 초단기실황[없음(0), 있음(1)], 초단기예보[없음(0), 낮음(1), 보통(2), 높음(3)] - 삭제
    private String SNO; //1시간 신적설, cm
    private String TMP; //1시간 기온, ℃
    private String PCP; //1시간 강수량, mm
    private String fcstDate; //예보 날짜
    private String fcstTime; //예보 시간
}
