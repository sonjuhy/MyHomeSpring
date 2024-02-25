package com.myhome.server;

import com.google.gson.Gson;
import com.myhome.server.api.dto.LoginDto;
import com.myhome.server.api.service.AuthService;
import com.myhome.server.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@Slf4j
@SpringBootTest
public class AuthTest {

    @Autowired
    AuthService authService;
    @Autowired
    UserService userService;

    @Autowired
    WebApplicationContext ctx;

    private MockMvc mockMvc;

    @BeforeEach
    public void mockSetUp(){
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    public void loginTest() throws Exception {
        LoginDto dto = new LoginDto("test","1234");
        MvcResult mvcResult = mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(dto))
                )
                .andReturn();
        log.info("loginTest result code : {}", mvcResult.getResponse().getStatus());
        log.info("loginTest result : {}", mvcResult.getResponse().getContentAsString());
    }
}
