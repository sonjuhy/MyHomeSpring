package com.myhome.server.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myhome.server.api.dto.LoginDto;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.AuthService;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
public class UserUnitServiceTest {
    @Rule
    public OutputCaptureRule outputCaptureRule = new OutputCaptureRule();

    @Mock
    AuthService authService;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Spy
    @InjectMocks
    UserServiceImpl userService;

    @DisplayName("signInSuccess")
    @Test
    public void signInSuccess(){
        // Given
        String id = "testId";
        String pw = "test1234!";

        LoginDto loginDto = LoginDto.builder()
                .id(id)
                .password(pw)
                .build();
        UserEntity userEntity = UserEntity.builder()
                .userDto(UserDto.builder()
                        .id("testId")
                        .auth("regular")
                        .build())
                .build();

        Claims claims = Jwts.claims().setSubject(id);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .setId("access")
                .compact();

        String sampleRefreshToken = Jwts.builder()
                .setClaims(claims)
                .setId("refresh")
                .compact();

        // When
        when(userService.findById(id)).thenReturn(Optional.ofNullable(userEntity));
        when(jwtTokenProvider.createToken(id, userEntity.getAuth(), true)).thenReturn(sampleAccessToken);
        when(jwtTokenProvider.createToken(id, userEntity.getAuth(), false)).thenReturn(sampleRefreshToken);

        // Then
        String signInResult = userService.signIn(loginDto);
        log.info("signInTestSuccess result : {}",signInResult);

        JsonObject jsonObject = JsonParser
                .parseString(signInResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("accessToken"));
        assertTrue(jsonObject.has("refreshToken"));
    }

    @DisplayName("signInFailedEntityIsNull")
    @Test
    public void signInFailedEntityIsNull(){
        // Given
        String id = "testId";
        String pw = "test1234!";

        LoginDto loginDto = LoginDto.builder()
                .id(id)
                .password(pw)
                .build();

        // When
        when(userService.findById(id)).thenReturn(Optional.empty());

        // Then
        String signInResult = userService.signIn(loginDto);
        log.info("signInFailedEntityIsNull result : {}",signInResult);

        JsonObject jsonObject = JsonParser
                .parseString(signInResult)
                .getAsJsonObject();

        assertFalse(jsonObject.has("accessToken"));
        assertFalse(jsonObject.has("refreshToken"));
        assertTrue(jsonObject.has("error"));
    }

    @DisplayName("signInFailedUpdateToken")
    @Test
    public void signInFailedUpdateToken(){
        // Given
        String id = "testId";
        String pw = "test1234!";

        LoginDto loginDto = LoginDto.builder()
                .id(id)
                .password(pw)
                .build();
        UserEntity userEntity = UserEntity.builder()
                .userDto(UserDto.builder()
                        .id("testId")
                        .auth("regular")
                        .build())
                .build();

        Claims claims = Jwts.claims().setSubject(id);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .setId("access")
                .compact();

        String sampleRefreshToken = Jwts.builder()
                .setClaims(claims)
                .setId("refresh")
                .compact();

        // When
        doThrow(new RuntimeException()).when(userService)
                .updateTokens(sampleAccessToken, sampleRefreshToken, id);
        when(userService.findById(id)).thenReturn(Optional.ofNullable(userEntity));
        when(jwtTokenProvider.createToken(id, userEntity.getAuth(), true)).thenReturn(sampleAccessToken);
        when(jwtTokenProvider.createToken(id, userEntity.getAuth(), false)).thenReturn(sampleRefreshToken);

        // Then
        String signInResult = userService.signIn(loginDto);
        log.info("signInFailedUpdateToken result : {}",signInResult);

        JsonObject jsonObject = JsonParser
                .parseString(signInResult)
                .getAsJsonObject();

        assertFalse(jsonObject.has("accessToken"));
        assertFalse(jsonObject.has("refreshToken"));
        assertTrue(jsonObject.has("error"));
    }


}
