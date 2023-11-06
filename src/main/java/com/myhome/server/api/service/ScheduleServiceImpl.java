package com.myhome.server.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    FileServerPrivateService fileServerPrivateService;

    @Autowired
    FileServerPublicService fileServerPublicService;

    @Scheduled(cron = "0 0 * * * *") // top of every hour of every day
    @Override
    public void sendRefreshReserveList() {
        kafkaProducer.sendReserveMessage();
    }

    @Scheduled(cron = "0 0 * * * *") // top of every hour of every day
    @Override
    public void checkCloudFile() {
        fileServerPublicService.publicFileStateCheck();
        fileServerPrivateService.privateFileCheck();
    }
}
