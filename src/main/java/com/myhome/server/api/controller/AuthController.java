package com.myhome.server.api.controller;

import com.myhome.server.api.dto.LoginDto;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.AuthService;
import com.myhome.server.api.service.UserService;
import com.myhome.server.db.entity.UserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService service;

    @Autowired
    AuthService authService;

    @Operation(description = "AccessToken 발급 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "accessToken 정상 발급"),
            @ApiResponse(responseCode = "401", description = "refreshToken 이 유효하지 않는 경우 - refreshToken 을 재발급 받아야 함")
    })
    @GetMapping("/getAccessToken/{refreshToken}") // re-create AccessToken with refresh Token
    public ResponseEntity<String> getAccessToken(@PathVariable String refreshToken){
        String result = authService.getAccessToken(refreshToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "유저 정보 발급 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : 유저 정보 정상 발급, 데이터 null : 유저 정보가 DB에 없음 - 회원 가입 해야 함"),
            @ApiResponse(responseCode = "401", description = "accessToken 이 유효하지 않는 경우 - refreshToken 을 재발급 받아야 함")
    })
    @GetMapping("/getUserInfo/{accessToken}") // get userInfo with accessToken
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable String accessToken){
        if(authService.validateAccessToken(accessToken)) {
            UserEntity entity = service.getUserInfo(accessToken);
            if (entity == null) return new ResponseEntity<>(null, HttpStatus.OK);
            return new ResponseEntity<>(entity, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(description = "accessToken 무결성 검사 결과 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true : 정상, false : 비정상(재발급 받아야 함)")
    })
    @GetMapping("/validateAccessToken/{accessToken}") // check validation of accessToken
    public ResponseEntity<Boolean> validateAccessToken(@PathVariable String accessToken){
        boolean result = authService.validateAccessToken(accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "refreshToken 무결성 검사 결과 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "true : 정상, false : 비정상(재발급 받아야 함)")
    })
    @GetMapping("/validateRefreshToken/{refreshToken}") // check validation of refreshToken
    public ResponseEntity<Boolean> validateRefreshToken(@PathVariable String refreshToken){
        boolean result = authService.validateRefreshToken(refreshToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "유저 권한 체크 하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : 유저 권한 체크 작동이 정상, 데이터 null : 백엔드 에러, 재시도 권유")
    })
    @GetMapping("/validateAuth/{accessToken}")
    public ResponseEntity<String> validateAuth(@PathVariable String accessToken){
        String result = authService.validateAuth(accessToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "accessToken 재발급 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : accessToken 재발급 작동이 정상, 데이터 null : 백엔드 에러, 재시도 권유")
    })
    @GetMapping("/reissueAccessToken/{accessToken}")
    public ResponseEntity<String> reissueAccessToken(@PathVariable String accessToken){
        String result = authService.reissueAccessToken(accessToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "refreshToken 재발급 받는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : refreshToken 재발급 작동이 정상, 데이터 null : 백엔드 에러, 재시도 권유")
    })
    @GetMapping("/reissueRefreshToken/{refreshToken}")
    public ResponseEntity<String> reissueRefreshToken(@PathVariable String refreshToken){
        String result = authService.reissueRefreshToken(refreshToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(description = "로그인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : 유저 정보 작동이 정상 발급, 데이터 null : 백엔드 에러, 재시도 권유")
    })
    @PostMapping("/signIn") // sign in
    public ResponseEntity<String> signIn(@RequestBody LoginDto dto){
        if(authService.checkPassword(dto.getPassword(), dto.getId())) {
            String result = service.signIn(dto);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.OK);
        }
    }

    @Operation(description = "회원 가입 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "데이터 존재 : 회원 가입 작동이 정상 처리, 데이터 null : 백엔드 에러, 재시도 권유")
    })
    @PostMapping("/signUp") // sign up
    public ResponseEntity<String> signUp(@RequestBody UserDto dto){
        String result = service.signUp(dto);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.OK);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
