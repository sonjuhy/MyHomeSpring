package com.myhome.server;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
//		StandardPBEStringEncryptor pbe = new StandardPBEStringEncryptor();
//		pbe.setAlgorithm("PBEWithMD5AndDES");
//		pbe.setPassword("myhomeProjectKey");
//
//		String enc = pbe.encrypt("sonjuhy_home");
//		System.out.println("enc : " + enc);
//
//		enc = pbe.encrypt("son278298@");
//		System.out.println("enc : " + enc);
//
//		enc = pbe.encrypt("jdbc:mysql://sonjuhy.iptime.org:3306/HomeUpdate?serverTimezone=Asia/Seoul");
//		System.out.println("enc : " + enc);
//		String des = pbe.decrypt(enc);
//		System.out.println("des : " + des);
	}

}
