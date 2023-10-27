package com.myhome.server;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.UserServiceImpl;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
@ActiveProfiles("...")
@TestPropertySource("classpath:application_test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class JPATests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void signInTest(){
//        assert userRepository.findByUserId("admin").isEmpty();
//        UserDto dto = new UserDto();
//        dto.setId(1);
//        dto.setName("admin");
//        dto.setPassword("1234");
//        dto.setRefreshToken("asdf123");
//        dto.setAccessToken("4312req");
//        dto.setUserId("admin");
//        dto.setAuth("admin");
//        Optional<UserEntity> entity = userService.findById(dto.getUserId());
//        assert entity.isEmpty();
    }
}
