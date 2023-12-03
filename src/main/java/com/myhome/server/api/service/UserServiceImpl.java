package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.component.LogComponent;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    UserRepository repository;

    @Autowired
    LogComponent logComponent;

    @Autowired
    FileServerPrivateService fileServerPrivateService;

    @Autowired
    FileDefaultPathRepository fileDefaultPathRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Override
    public UserEntity findByUserId(long userId) {
        UserEntity entity = repository.findByUserId(userId);
        return entity;
    }

    @Override
    public Optional<UserEntity> findById(String id) {
        Optional<UserEntity> entity = repository.findById(id);
        return entity;
    }

    @Override
    public List<UserEntity> findAll() {
        List<UserEntity> list = repository.findAll();
        return list;
    }

    @Override
    public int updateUser(UserDto userDto) {
        UserEntity entity = repository.save(new UserEntity(userDto));
        System.out.println(entity.toString());
        return 1;
    }

    @Override
    public void updateTokens(String accessToken, String refreshToken, String Id) {
        repository.updateTokens(accessToken, refreshToken, Id);
    }


    @Transactional
    @Override
    public boolean checkPassword(String inputPassword, String id) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(inputPassword, loadUserByUsername(id).getPassword());
    }

    @Override
    public String getAccessToken(String refreshToken) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();

        boolean result = jwtTokenProvider.validateToken(refreshToken); // check accessToken validate
        if(result){
            String userId = jwtTokenProvider.getUserPk(refreshToken); // get userId from refreshToken
            Optional<UserEntity> entity = findById(userId); // get userInfo with refreshToken for getting authList

            String newAccessToken = jwtTokenProvider.createToken(userId, entity.get().getAuth(), true); // re-create AccessToken
            jsonObject.addProperty("accessToken", newAccessToken); //save accessToken
        }
        else{
            jsonObject.addProperty("error","need login again"); //expired refreshToken
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public UserEntity getUserInfo(String accessToken) {
        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = findById(userId); // get userInfo with userId
        return optionalUserEntity.orElse(null);
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
        Optional<UserEntity> optionalUserEntity = findById(userId); // get userInfo with userId
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
        Optional<UserEntity> optionalUserEntity = findById(userId); // get userInfo with userId
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
        Optional<UserEntity> optionalUserEntity = findById(userId); // get userInfo with userId
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

    @Override
    public String signIn(UserDto dto) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        if(findById(dto.getId()).isEmpty()){ // not user in this service
            jsonObject.addProperty("error","this Id is not user");
        }
        else {
            boolean loginResult = checkPassword(dto.getPassword(), dto.getId()); // check login info
            if(loginResult){ // password match result is correct
                String accessToken = jwtTokenProvider.createToken(dto.getId(), dto.getAuth(), true);
                String refreshToken = jwtTokenProvider.createToken(dto.getId(), dto.getAuth(), false);
                jsonObject.addProperty("accessToken", accessToken);
                jsonObject.addProperty("refreshToken", refreshToken);
                dto.setAccessToken(accessToken);
                dto.setRefreshToken(accessToken);
                dto.setPassword(null);
                try{
                    updateTokens(accessToken, refreshToken, dto.getId()); // update token data
                }
                catch(Exception e){
//                    logComponent.sendErrorLog("serviceName", "[signIn] content : ", e, "topic");
                    jsonObject.remove("accessToken");
                    jsonObject.remove("refreshToken");
                    jsonObject.addProperty("error", "failed to update user info");
                    return gson.toJson(jsonObject);
                }
            }
            else{ // password match result is not correct
                jsonObject.addProperty("error","incorrect password");
                return gson.toJson(jsonObject);
            }
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public String signUp(UserDto dto) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        Optional<UserEntity> entity = findById(dto.getId()); // search user info with userDto data
        if(entity.isPresent()){ // already exist user data
            jsonObject.addProperty("error","already exist user");
        }
        else{
            try{
                String accessToken = jwtTokenProvider.createToken(dto.getId(), dto.getAuth(), true);
                String refreshToken = jwtTokenProvider.createToken(dto.getId(), dto.getAuth(), false);
                dto.setAccessToken(accessToken);
                dto.setRefreshToken(refreshToken);
                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                String cryptPW = bCryptPasswordEncoder.encode(dto.getPassword()); // encoding pw with bCrypt
                dto.setPassword(cryptPW); // setting encoding pw

                updateUser(dto); // save(update) userInfo

                jsonObject.addProperty("accessToken", accessToken);
                jsonObject.addProperty("refreshToken", refreshToken);

                FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");
                fileServerPrivateService.mkdir(storeEntity.getPrivateDefaultPath()+ File.separator+dto.getId(), accessToken);
            }
            catch(Exception e){
//                logComponent.sendErrorLog("serviceName", "[signUp] content : ", e, "topic");
                jsonObject = new JsonObject();
                jsonObject.addProperty("error","failed to update user info");
                return gson.toJson(jsonObject);
            }
        }
        return gson.toJson(jsonObject);
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        System.out.println("loadUserByUserName : " + repository.findById(id));
        return repository.findById(id).orElseThrow(()->new UsernameNotFoundException(id));
    }
}
