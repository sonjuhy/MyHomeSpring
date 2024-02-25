package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.UserEntity;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    UserService userService;

    @Override
    public void updateTokens(String accessToken, String refreshToken, String id) {
        // call userService
    }

    @Override
    public boolean checkPassword(String inputPassword, String id) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        Optional<UserEntity> entity = userService.findById(id);
        return entity.filter(userEntity -> passwordEncoder.matches(inputPassword, userEntity.getPassword())).isPresent();
    }

    @Override
    public String getAccessToken(String refreshToken) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        boolean result = jwtTokenProvider.validateToken(refreshToken); // check accessToken validate
        if(result){
            String userId = jwtTokenProvider.getUserPk(refreshToken); // get userId from refreshToken
            Optional<UserEntity> entity = userService.findById(userId); // get userInfo with refreshToken for getting authList

            String newAccessToken = jwtTokenProvider.createToken(userId, entity.get().getAuth(), true); // re-create AccessToken
            jsonObject.addProperty("accessToken", newAccessToken); //save accessToken
        }
        else{
            jsonObject.addProperty("error","need login again"); //expired refreshToken
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public boolean validateAccessToken(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken); // check validation
    }

    @Override
    public boolean validateRefreshToken(String refreshToken) {
        return jwtTokenProvider.validateToken(refreshToken); // check validation
    }

    @Override
    public String validateAuth(String accessToken) {
        String newAccessToken = "";
        boolean auth = false, accessTokenValidate;
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = userService.findById(userId); // get userInfo with userId

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
            updateTokens(newAccessToken, entity.getRefreshToken(), entity.getId());
        }
        catch(Exception e){
//            logComponent.sendErrorLog("serviceName", "[validateAuth] content : ", e, "topic");
            jsonObject.addProperty("error", "failed to update token info");
            return gson.toJson(jsonObject);
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public String reissueAccessToken(String accessToken) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = userService.findById(userId); // get userInfo with userId
        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        String newAccessToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),true);

        try{
            updateTokens(newAccessToken, entity.getRefreshToken(), entity.getId());
            jsonObject.addProperty("accessToken", newAccessToken);
        }
        catch(Exception e){
//            logComponent.sendErrorLog("serviceName", "[reissueAccessToken] content : ", e, "topic");
            jsonObject.addProperty("error", "failed");
            return gson.toJson(jsonObject);
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public String reissueRefreshToken(String refreshToken) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        String userId = jwtTokenProvider.getUserPk(refreshToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = userService.findById(userId); // get userInfo with userId
        UserEntity entity = optionalUserEntity.orElseThrow(()->new RuntimeException(userId)); // get UserEntity when Optional Entity is present
        String newAccessToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),true);
        String newRefreshToken = jwtTokenProvider.createToken(entity.getId(), entity.getAuth(),false);

        try{
            updateTokens(newAccessToken, newRefreshToken, entity.getId());
            jsonObject.addProperty("accessToken", newAccessToken);
            jsonObject.addProperty("refreshToken", newRefreshToken);
        }
        catch(Exception e){
//            logComponent.sendErrorLog("serviceName", "[reissueRefreshToken] content : ", e, "topic");
            jsonObject.addProperty("error", "failed");
            return gson.toJson(jsonObject);
        }
        return gson.toJson(jsonObject);
    }
}
