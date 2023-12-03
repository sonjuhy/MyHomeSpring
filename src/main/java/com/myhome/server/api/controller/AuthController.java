package com.myhome.server.api.controller;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.UserService;
import com.myhome.server.db.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService service;

    @GetMapping("/getAccessToken/{refreshToken}") // re-create AccessToken with refresh Token
    public ResponseEntity<String> getAccessToken(@PathVariable String refreshToken){
        String result = service.getAccessToken(refreshToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/getUserInfo/{accessToken}") // get userInfo with accessToken
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable String accessToken){
        UserEntity entity = service.getUserInfo(accessToken);
        if(entity == null) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @GetMapping("/validateAccessToken/{accessToken}") // check validation of accessToken
    public ResponseEntity<Boolean> validateAccessToken(@PathVariable String accessToken){
        boolean result = service.validateAccessToken(accessToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/validateRefreshToken/{refreshToken}") // check validation of refreshToken
    public ResponseEntity<Boolean> validateRefreshToken(@PathVariable String refreshToken){
        boolean result = service.validateRefreshToken(refreshToken);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/validateAuth/{accessToken}")
    public ResponseEntity<String> validateAuth(@PathVariable String accessToken){
        String result = service.validateAuth(accessToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/reissueAccessToken/{accessToken}")
    public ResponseEntity<String> reissueAccessToken(@PathVariable String accessToken){
        String result = service.reissueAccessToken(accessToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/reissueRefreshToken/{refreshToken}")
    public ResponseEntity<String> reissueRefreshToken(@PathVariable String refreshToken){
        String result = service.reissueRefreshToken(refreshToken);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/signIn") // sign in
    public ResponseEntity<String> signIn(@RequestBody UserDto userDto){
        String result = service.signIn(userDto);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/signUp") // sign up
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto){
        String result = service.signUp(userDto);
        if(result == null || result.isEmpty()) return new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
