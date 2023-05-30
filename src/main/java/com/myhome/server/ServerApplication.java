package com.myhome.server;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@IntegrationComponentScan
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
//		StandardPBEStringEncryptor pbe = new StandardPBEStringEncryptor();
//		pbe.setAlgorithm("PBEWithMD5AndDES");
//		pbe.setPassword("myhomeProjectKey");
//
//		String enc = pbe.encrypt("sonjuhy_home");

	}

}
