package com.myhome.server.config;

import com.myhome.server.config.jwt.JwtTokenProvider;
import com.myhome.server.db.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@RunWith(SpringRunner.class)
public class JWTUnitTest {

    @InjectMocks
    JwtTokenProvider jwtTokenProvider;
    @Mock
    UserRepository userRepository;

    @DisplayName("CreateToken")
    @Test
    public void createTokenSuccess(){
        // Given
        String pk = "testPk";
        String roles = "regular";
        boolean choice = true;

        // When
        String token = jwtTokenProvider.createToken(pk, roles, choice);

        // Then
        assertNotNull(token);
    }
}
