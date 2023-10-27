package com.myhome.server;

import com.myhome.server.api.controller.AuthController;
import com.myhome.server.api.dto.UserDto;
import com.myhome.server.db.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServerApplicationTests {

	@Autowired
	private AuthController authController;

	@Test
	void contextLoads() {
		assert authController != null;
	}

}
