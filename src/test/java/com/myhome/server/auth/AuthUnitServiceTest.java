package com.myhome.server.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.AuthServiceImpl;
import com.myhome.server.api.service.UserService;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
public class AuthUnitServiceTest {

    @Rule
    public OutputCaptureRule outputCaptureRule = new OutputCaptureRule();

    @Spy
    @InjectMocks
    AuthServiceImpl authService;
    @Mock
    UserRepository userRepository;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    UserService userService;


    @DisplayName("PasswordCheckSuccess")
    @Test
    public void checkPasswordSuccess(){
        // Given
        String id = "testId";
        String pw = "test1234!";
        String encryptionPW = new BCryptPasswordEncoder().encode(pw);
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                        .userDto(UserDto.builder()
                                .id(id)
                                .password(encryptionPW)
                                .build())
                        .build());

        // When
        when(userService.findById(id)).thenReturn(entity);
        boolean checkResult = authService.checkPassword(pw, id);

        // Then
//        assertThat(outputCaptureRule.toString().contains("authService"));
        assertTrue(checkResult);
    }

    @DisplayName("PasswordCheckFailed")
    @Test
    public void checkPasswordFailed(){
        // Given
        String id = "testId";
        String pw = "test1234!";
        String wrongPW = "wrong1234!";
        String encryptionPW = new BCryptPasswordEncoder().encode(wrongPW);
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(id)
                        .password(encryptionPW)
                        .build())
                .build());

        // When
        when(userService.findById(id)).thenReturn(entity);
        boolean checkResult = authService.checkPassword(pw, id);

        // Then
        assertFalse(checkResult);
    }

    @DisplayName("GetAccessTokenSuccess")
    @Test
    public void getAccessTokenSuccess(){
        // Given
        String sampleToken = "sampleToken";
        String userId = "testId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .build())
                .build());

        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.validateToken(sampleToken)).thenReturn(true);
        when(jwtTokenProvider.getUserPk(sampleToken)).thenReturn(userId);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String resultString = authService.getAccessToken(sampleToken);
        System.out.println(resultString);
        log.info("GetAccessTokenSuccess resultString : {}", resultString);

        // Then
        assertNotNull(resultString);
        assertTrue(JsonParser
                .parseString(resultString)
                .getAsJsonObject()
                .has("accessToken")
        );
    }

    @DisplayName("GetAccessTokenValidateFailed")
    @Test
    public void getAccessTokenValidateFailed(){
        // Given
        String sampleToken = "sampleToken";
        String wrongSampleToken = "wrongToken";
        String userId = "testId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .build())
                .build());

        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.validateToken(sampleToken)).thenReturn(true);
        when(jwtTokenProvider.getUserPk(sampleToken)).thenReturn(userId);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String resultString = authService.getAccessToken(wrongSampleToken);
        System.out.println(resultString);

        // Then
        assertNotNull(resultString);
        assertFalse(JsonParser
                .parseString(resultString)
                .getAsJsonObject()
                .has("accessToken")
        );
    }

    @DisplayName("ValidateCheckAboutAccessToken")
    @Test
    public void validateAuthSuccess(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.validateAuth(accessToken);
        log.info("ValidateCheckAboutAccessToken validateResult : {}", validateResult);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("authValidate"));
        assertTrue(jsonObject.has("accessTokenValidate"));
        assertTrue(jsonObject.has("newAccessToken"));

        assertTrue(jsonObject.get("authValidate").getAsBoolean());
        assertTrue(jsonObject.get("accessTokenValidate").getAsBoolean());

        assertEquals(jsonObject.get("newAccessToken").getAsString(), accessToken, "compare accessToken");
    }

    @DisplayName("validateAuthFailedAccessTokenAssociate")
    @Test
    public void validateAuthFailedAccessTokenNotAssociate(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        String userAuth = "associate";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth(userAuth)
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);
        when(jwtTokenProvider.createToken(userId, userAuth, true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.validateAuth(accessToken);
        log.info("ValidateCheckAboutAccessToken validateResult : {}", validateResult);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("authValidate"));
        assertTrue(jsonObject.has("accessTokenValidate"));
        assertTrue(jsonObject.has("newAccessToken"));

        assertFalse(jsonObject.get("authValidate").getAsBoolean());
        assertFalse(jsonObject.get("accessTokenValidate").getAsBoolean());

        assertEquals(jsonObject.get("newAccessToken").getAsString(), sampleAccessToken);
    }

    @DisplayName("validateAuthFailedAccessTokenNotValidate")
    @Test
    public void validateAuthFailedAccessTokenNotValidate(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.validateAuth(accessToken);
        log.info("ValidateCheckAboutAccessToken validateResult : {}, sampleAccessToken : {}", validateResult, accessToken);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("authValidate"));
        assertTrue(jsonObject.has("accessTokenValidate"));
        assertTrue(jsonObject.has("newAccessToken"));

        assertTrue(jsonObject.get("authValidate").getAsBoolean());
        assertFalse(jsonObject.get("accessTokenValidate").getAsBoolean());

        assertNotSame(jsonObject.get("newAccessToken").getAsString(), sampleAccessToken);
    }

    @DisplayName("validateAuthFailedUpdateToken")
    @Test
    public void validateAuthFailedUpdateToken(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .refreshToken("sampleRefreshToken")
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        doThrow(new RuntimeException()).when(authService)
                .updateTokens(sampleAccessToken, entity.get().getRefreshToken(), userId);
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.validateToken(accessToken)).thenReturn(false);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.validateAuth(accessToken);
        log.info("validateAuthFailedUpdateToken validateResult : {}", validateResult);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("error"));
        assertFalse(jsonObject.has("authValidate"));
        assertFalse(jsonObject.has("accessTokenValidate"));
        assertFalse(jsonObject.has("newAccessToken"));
    }

    @DisplayName("reissueAccessTokenSuccess")
    @Test
    public void reissueAccessTokenSuccess(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .refreshToken("sampleRefreshToken")
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.reissueAccessToken(accessToken);
        log.info("reissueAccessTokenSuccess validateResult : {}", validateResult);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("accessToken"));
    }

    @DisplayName("reissueAccessTokenFailedUpdateToken")
    @Test
    public void reissueAccessTokenFailedUpdateToken(){
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "userId";
        Optional<UserEntity> entity = Optional.of(UserEntity.builder()
                .userDto(UserDto.builder()
                        .id(userId)
                        .auth("regular")
                        .refreshToken("sampleRefreshToken")
                        .build())
                .build());
        Claims claims = Jwts.claims().setSubject(userId);
        String sampleAccessToken = Jwts.builder()
                .setClaims(claims)
                .compact();

        // When
        doThrow(new RuntimeException()).when(authService)
                .updateTokens(sampleAccessToken, entity.get().getRefreshToken(), userId);
        when(jwtTokenProvider.getUserPk(accessToken)).thenReturn(userId);
        when(jwtTokenProvider.createToken(userId, "regular", true)).thenReturn(sampleAccessToken);
        when(userService.findById(userId)).thenReturn(entity);

        String validateResult = authService.reissueAccessToken(accessToken);
        log.info("reissueAccessTokenFailedUpdateToken validateResult : {}", validateResult);

        // Then
        assertNotNull(validateResult);
        JsonObject jsonObject = JsonParser
                .parseString(validateResult)
                .getAsJsonObject();

        assertTrue(jsonObject.has("error"));
    }
}
