package com.myhome.server.api.controller;

import com.myhome.server.api.dto.NoticeDto;
import com.myhome.server.api.service.NoticeService;
import com.myhome.server.api.service.NoticeServiceImpl;
import com.myhome.server.db.entity.NoticeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService = new NoticeServiceImpl();

    @GetMapping("/getTopNotice")
    public ResponseEntity<NoticeEntity> getTopNotice(){
        NoticeEntity entity = noticeService.findTopNotice();
        return new ResponseEntity<>(entity, HttpStatus.OK);
    }

    @PostMapping("/setNotice")
    public ResponseEntity<String> setNotice(@RequestBody NoticeEntity entity){
        noticeService.save(entity);
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }
}
