package com.myhome.server.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Slf4j
@Transactional
@SpringBootTest
public class AuthTest {
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
    public void loginSuccessTest() throws Exception {
        // Given
        LoginDto dto = new LoginDto("test","1234");

        // When
        MvcResult mvcResult = mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(dto))
                )
                .andReturn();

        // Then
        JsonObject jsonObject = JsonParser
                .parseString(mvcResult.getResponse().getContentAsString())
                .getAsJsonObject();

        assertEquals(200, mvcResult.getResponse().getStatus());

        assertTrue(jsonObject.has("accessToken"));
        assertTrue(jsonObject.has("refreshToken"));
    }

    @Test
    public void loginTestFailedWrongInfo() throws Exception {
        // Given
        LoginDto dto = new LoginDto("wrongId","wrong1234!");

        // When
        MvcResult mvcResult = mockMvc.perform(post("/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new Gson().toJson(dto))
                )
                .andReturn();

        // Then
        assertEquals(200, mvcResult.getResponse().getStatus());
        assertEquals(mvcResult.getResponse().getContentAsString(), "");
    }
}
