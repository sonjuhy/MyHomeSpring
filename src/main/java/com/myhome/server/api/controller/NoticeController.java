package com.myhome.server.api.controller;

import com.myhome.server.api.service.NoticeService;
import com.myhome.server.api.service.NoticeServiceImpl;
import com.myhome.server.api.service.UserService;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.entity.NoticeEntity;
import com.myhome.server.db.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService = new NoticeServiceImpl();

    @Autowired
    private UserService userService = new UserServiceImpl();

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getTopNotice")
    public ResponseEntity<NoticeEntity> getTopNotice(){
        NoticeEntity entity = noticeService.findTopNotice();
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @GetMapping("/getAllNotice")
    public ResponseEntity<List<NoticeEntity>> getAllNotice(){
        List<NoticeEntity> list = noticeService.findAll();
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/getNoticeByWriter")
    public ResponseEntity<List<NoticeEntity>> getNoticeByWriter(@RequestParam String token){
        String userPK = jwtTokenProvider.getUserPk(token);
        Optional<UserEntity> userEntity = userService.findById(userPK);
        List<NoticeEntity> list = noticeService.findByWriter(userEntity.get().getName());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/setNotice")
    public ResponseEntity<String> setNotice(@RequestBody NoticeEntity entity){
        noticeService.save(entity);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    @PutMapping("/updateNotice")
    public ResponseEntity<Integer> updateNotice(@RequestBody NoticeEntity entity){
        noticeService.save(entity);
        return new ResponseEntity<>(0, HttpStatus.OK);
    }

    @DeleteMapping("/deleteNotice")
    public ResponseEntity<Integer> deleteNotice(@RequestParam int pk){
        noticeService.delete(pk);
        return new ResponseEntity<>(0, HttpStatus.OK);
    }
}
