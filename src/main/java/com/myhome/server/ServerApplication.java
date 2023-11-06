package com.myhome.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;
import java.sql.Time;
import java.util.TimeZone;

@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
//		StandardPBEStringEncryptor pbe = new StandardPBEStringEncryptor();
//		pbe.setAlgorithm("PBEWithMD5AndDES");
//		pbe.setPassword("myhomeProjectKey");
//
//		String enc = pbe.encrypt("sonjuhy_home");

	}

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
	}
}
