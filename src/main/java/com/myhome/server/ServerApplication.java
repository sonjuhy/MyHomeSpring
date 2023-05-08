package com.myhome.server;

import com.myhome.server.config.mqtt.MQTTConfig;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.integration.annotation.IntegrationComponentScan;

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
