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
    private NoticeService noticeService;

    @Autowired
    private UserService userService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @GetMapping("/getTopNotice")
    public ResponseEntity<NoticeEntity> getTopNotice(){
        NoticeEntity entity = noticeService.findTopNotice();
        if(entity != null) return new ResponseEntity<>(entity, HttpStatus.OK);
        else return new ResponseEntity<>(entity, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getAllNotice")
    public ResponseEntity<List<NoticeEntity>> getAllNotice(){
        List<NoticeEntity> list = noticeService.findAll();
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/getNoticeByWriter")
    public ResponseEntity<List<NoticeEntity>> getNoticeByWriter(@RequestParam String token){
        String userPK = jwtTokenProvider.getUserPk(token);
        if(userPK == null) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);

        Optional<UserEntity> userEntity = userService.findById(userPK);
        if(userEntity.isEmpty()) return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);

        List<NoticeEntity> list = noticeService.findByWriter(userEntity.get().getName());
        if(list != null && list.size() > 0) return new ResponseEntity<>(list, HttpStatus.OK);
        else return new ResponseEntity<>(list, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/setNotice")
    public ResponseEntity<Integer> setNotice(@RequestBody NoticeEntity entity){
        int result = noticeService.save(entity);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("/updateNotice")
    public ResponseEntity<Integer> updateNotice(@RequestBody NoticeEntity entity){
        int result = noticeService.save(entity);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/deleteNotice")
    public ResponseEntity<Integer> deleteNotice(@RequestParam int pk){
        int result = noticeService.delete(pk);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
