package com.myhome.server.api.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.myhome.server.api.dto.FileServerPrivateDto;
import com.myhome.server.api.dto.LoginDto;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.component.LogComponent;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerPrivateEntity;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerPrivateRepository;
import com.myhome.server.db.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
//public class UserServiceImpl implements UserService, UserDetailsService {
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository repository;

    @Autowired
    FileServerPrivateRepository fileServerPrivateRepository;

    @Autowired
    LogComponent logComponent;

    @Autowired
    FileServerCommonService fileServerCommonService;

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
    public UserEntity findByAccessToken(String accessToken) {
        Optional<UserEntity> entity = repository.findByAccessToken(accessToken);
        return entity.orElse(null);
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

    @Override
    public UserEntity getUserInfo(String accessToken) {
        String userId = jwtTokenProvider.getUserPk(accessToken); // get userId from accessToken
        Optional<UserEntity> optionalUserEntity = findById(userId); // get userInfo with userId
        return optionalUserEntity.orElse(null);
    }

    @Override
    public String signIn(LoginDto dto) {
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        Optional<UserEntity> userEntity = findById(dto.getId());
        if(!userEntity.isPresent()){ // not user in this service
            jsonObject.addProperty("error","this Id is not user");
        }
        else {
//            boolean loginResult = checkPassword(dto.getPassword(), dto.getId()); // check login info
            UserEntity user = userEntity.get();

            String accessToken = jwtTokenProvider.createToken(dto.getId(), user.getAuth(), true);
            String refreshToken = jwtTokenProvider.createToken(dto.getId(), user.getAuth(), false);
            jsonObject.addProperty("accessToken", accessToken);
            jsonObject.addProperty("refreshToken", refreshToken);

            UserDto userDto = new UserDto();
            userDto.setAccessToken(accessToken);
            userDto.setRefreshToken(accessToken);
            userDto.setPassword(null);
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
                String accessToken = jwtTokenProvider.createToken(dto.getId(), "associate", true);
                String refreshToken = jwtTokenProvider.createToken(dto.getId(), "associate", false);

                dto.setAccessToken(accessToken);
                dto.setRefreshToken(refreshToken);
                dto.setAuth("associate");

                BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
                String cryptPW = bCryptPasswordEncoder.encode(dto.getPassword()); // encoding pw with bCrypt
                dto.setPassword(cryptPW); // setting encoding pw

                updateUser(dto); // save(update) userInfo

                jsonObject.addProperty("accessToken", accessToken);
                jsonObject.addProperty("refreshToken", refreshToken);

                FileDefaultPathEntity storeEntity = fileDefaultPathRepository.findByPathName("store");

                String path = storeEntity.getPrivateDefaultPath()+ File.separator+dto.getId();
                String originPath = fileServerCommonService.changeUnderBarToSeparator(path);
                File file = new File(originPath);
                if(file.mkdir()){
                    String underBar = "__";
                    String[] paths = path.split(underBar);
                    String name = paths[paths.length-1];
                    StringBuilder location = new StringBuilder();
                    for(int i=0;i<paths.length-1;i++){
                        location.append(paths[i]).append(underBar);
                    }
                    String uuid = UUID.nameUUIDFromBytes(path.getBytes(StandardCharsets.UTF_8)).toString();
                    FileServerPrivateDto serverPrivateDto = new FileServerPrivateDto(
                            path, // file path (need to change)
                            name, // file name
                            uuid, // file name to change UUID
                            "dir",
                            0, // file size(KB)
                            dto.getName(),
                            location.toString(), // file folder path (need to change)
                            0,
                            0
                    );
                    fileServerPrivateRepository.save(new FileServerPrivateEntity(serverPrivateDto));
//                    logComponent.sendLog("Cloud", "[mkdir(private)] mkdir dto : "+dto, true, TOPIC_CLOUD_LOG);
                }
                else{
//                    logComponent.sendLog("Cloud", "[mkdir(private)] failed to mkdir (path) : "+path, false, TOPIC_CLOUD_LOG);
                }
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
}
