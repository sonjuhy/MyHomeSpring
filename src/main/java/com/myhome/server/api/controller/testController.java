package com.myhome.server.api.controller;

import com.myhome.server.api.service.UserService;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {
    @Autowired
    UserService service = new UserServiceImpl();
    @GetMapping("/test/{number}")
    public String testfun(@PathVariable int number){
        UserEntity entity = service.findByUserId(number);
        return entity.toString();
    }
}
