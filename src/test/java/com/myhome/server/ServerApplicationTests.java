package com.myhome.server;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.api.service.FileServerThumbNailService;
import com.myhome.server.db.entity.FileDefaultPathEntity;
import com.myhome.server.db.entity.FileServerThumbNailEntity;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.FileDefaultPathRepository;
import com.myhome.server.db.repository.FileServerThumbNailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ServerApplicationTests {

	@Autowired
	private FileDefaultPathRepository fileDefaultPathRepository;

	@Autowired
	private FileServerThumbNailRepository fileServerThumbNailRepository;


	@Test
	public void getDefaultPath(){
		FileDefaultPathEntity entity = fileDefaultPathRepository.findByPathName("store");
		System.out.println("test getDefaultPath : "+entity.getPublicDefaultPath());
	}

	@Test
	public void getThumbnailWithoutFile(){
		List<FileServerThumbNailEntity> list = fileServerThumbNailRepository.findAllNotInPublic();
		System.out.println("test getThumbnailWithoutFile : "+list);
	}
}
