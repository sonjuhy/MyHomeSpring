package com.myhome.server.api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.UserService;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UserService service;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getAccessToken/{refreshToken}") // re-create AccessToken with refresh Token
    public ResponseEntity<String> getAccessToken(@PathVariable String refreshToken){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        boolean result = jwtTokenProvider.validateToken(refreshToken); // check accessToken validate
        if(result){
            String userId = jwtTokenProvider.getUserPk(refreshToken); // get userId from refreshToken
            Optional<UserEntity> entity = service.findById(userId); // get userInfo with refreshToken for getting authList

            String newAccessToken = jwtTokenProvider.createToken(userId, entity.get().getAuth(), true); // re-create AccessToken
            jsonObject.addProperty("accessToken", newAccessToken); //save accessToken
        }
        else{
            jsonObject.addProperty("error","need login again"); //expired refreshToken
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }

    @GetMapping("/getUserInfo/{accessToken}") // get userInfo with accessToken
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable String accessToken){
        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = service.findById(userId); // get userInfo with userId
//        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        if(optionalUserEntity.isPresent()){
            UserEntity entity = optionalUserEntity.get();
            return new ResponseEntity<>(entity, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/validateAccessToken/{accessToken}") // check validation of accessToken
    public ResponseEntity<Boolean> validateAccessToken(@PathVariable String accessToken){
        boolean result = jwtTokenProvider.validateToken(accessToken); // check validation
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/validateRefreshToken/{refreshToken}") // check validation of refreshToken
    public ResponseEntity<Boolean> validateRefreshToken(@PathVariable String refreshToken){
        boolean result = jwtTokenProvider.validateToken(refreshToken); // check validation
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/validateAuth/{accessToken}")
    public ResponseEntity<String> validateAuth(@PathVariable String accessToken){
        String newAccessToken = "";
        boolean auth = false, accessTokenValidate;
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = service.findById(userId); // get userInfo with userId
        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        if(!entity.getAuth().equals("associate")){
            auth = true;
        }
        accessTokenValidate = jwtTokenProvider.validateToken(accessToken);
        if(!accessTokenValidate){
            newAccessToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),true);
        }
        else{
            newAccessToken = accessToken;
        }

        jsonObject.addProperty("authValidate", auth);
        jsonObject.addProperty("accessTokenValidate", accessTokenValidate);
        jsonObject.addProperty("newAccessToken", newAccessToken);

        try{
            service.updateTokens(newAccessToken, entity.getRefreshToken(), entity.getId());
        }
        catch(Exception e){
            e.printStackTrace();
            jsonObject.addProperty("error", "failed to update token info");
            return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }

    @GetMapping("/reissueAccessToken/{accessToken}")
    public ResponseEntity<String> reissueAccessToken(@PathVariable String accessToken){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = service.findById(userId); // get userInfo with userId
        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        String newAccessToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),true);

        try{
            service.updateTokens(newAccessToken, entity.getRefreshToken(), entity.getId());
            jsonObject.addProperty("accessToken", newAccessToken);
        }
        catch(Exception e){
            e.printStackTrace();
            jsonObject.addProperty("error", "failed");
            return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }

    @GetMapping("/reissueRefreshToken/{refreshToken}")
    public ResponseEntity<String> reissueRefreshToken(@PathVariable String refreshToken){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        String userId = jwtTokenProvider.getUserPk(refreshToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = service.findById(userId); // get userInfo with userId
        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        String newAccessToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),true);
        String newRefreshToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),false);

        try{
            service.updateTokens(newAccessToken, newRefreshToken, entity.getId());
            jsonObject.addProperty("accessToken", newAccessToken);
            jsonObject.addProperty("refreshToken", newRefreshToken);
        }
        catch(Exception e){
            e.printStackTrace();
            jsonObject.addProperty("error", "failed");
            return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }

    @PostMapping("/signIn") // sign in
    public ResponseEntity<String> signIn(@RequestBody UserDto userDto){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        if(service.findById(userDto.getId()).isEmpty()){ // not user in this service
            jsonObject.addProperty("error","this Id is not user");
        }
        else {
            boolean loginResult = service.checkPassword(userDto.getPassword(), userDto.getId()); // check login info
            if(loginResult){ // password match result is correct
                String accessToken = jwtTokenProvider.createToken(userDto.getId(), userDto.getAuth(), true);
                String refreshToken = jwtTokenProvider.createToken(userDto.getId(), userDto.getAuth(), false);
                jsonObject.addProperty("accessToken", accessToken);
                jsonObject.addProperty("refreshToken", refreshToken);
                userDto.setAccessToken(accessToken);
                userDto.setRefreshToken(accessToken);
                userDto.setPassword(null);
                try{
                    service.updateTokens(accessToken, refreshToken, userDto.getId()); // update token data
                }
                catch(Exception e){
                    e.printStackTrace();
                    jsonObject.remove("accessToken");
                    jsonObject.remove("refreshToken");
                    jsonObject.addProperty("error", "failed to update user info");
                    return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
                }
            }
            else{ // password match result is not correct
                jsonObject.addProperty("error","incorrect password");
                return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
            }
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }

    @PostMapping("/signUp") // sign up
    public ResponseEntity<String> signUp(@RequestBody UserDto userDto){
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        Optional<UserEntity> entity = service.findById(userDto.getId()); // search user info with userDto data
        if(entity.isPresent()){ // already exist user data
            jsonObject.addProperty("error","already exist user");
        }
        else{
            try{
                String accessToken = jwtTokenProvider.createToken(userDto.getId(), userDto.getAuth(), true);
                String refreshToken = jwtTokenProvider.createToken(userDto.getId(), userDto.getAuth(), false);
                userDto.setAccessToken(accessToken);
                userDto.setRefreshToken(refreshToken);
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                String cryptPW = bCryptPasswordEncoder.encode(userDto.getPassword()); // encoding pw with bCrypt
                userDto.setPassword(cryptPW); // setting encoding pw

                service.updateUser(userDto); // save(update) userInfo

                jsonObject.addProperty("accessToken", accessToken);
                jsonObject.addProperty("refreshToken", refreshToken);
                // return access, refresh token
            }
            catch(Exception e){
                e.printStackTrace();
                jsonObject = new JsonObject();
                jsonObject.addProperty("error","failed to update user info");
                return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.BAD_GATEWAY);
            }
        }
        return new ResponseEntity<>(gson.toJson(jsonObject), HttpStatus.OK);
    }
}
