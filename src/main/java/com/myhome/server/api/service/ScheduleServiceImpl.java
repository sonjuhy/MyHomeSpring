package com.myhome.server.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    FileServerPrivateService fileServerPrivateService;

    @Autowired
    FileServerPublicService fileServerPublicService;

    @Scheduled(cron = "0 0 0 * * *") // top of  every day (second, min, hour, day, month, week)
    @Override
    public void sendRefreshReserveList() {
        kafkaProducer.sendReserveMessage();
    }

    @Scheduled(cron = "0 0 0 * * *") // top of e every day
    @Override
    public void checkCloudFile() {
        try {
            fileServerPublicService.publicFileStateCheck();
            kafkaProducer.sendLogDto("Cloud-Check", "[checkCloudFile(public)] check success", true, TOPIC_CLOUD_CHECK_LOG);
        }
        catch (Exception e){
            kafkaProducer.sendLogDto("Cloud-Check", "[checkCloudFile(public)] error : "+e.getMessage(), false, TOPIC_CLOUD_CHECK_LOG);
        }
        try {
            fileServerPrivateService.privateFileCheck();
            kafkaProducer.sendLogDto("Cloud-Check", "[checkCloudFile(private)] check success", true, TOPIC_CLOUD_CHECK_LOG);
        }
        catch (Exception e){
            kafkaProducer.sendLogDto("Cloud-Check", "[checkCloudFile(private)] error : "+e.getMessage(), false, TOPIC_CLOUD_CHECK_LOG);
        }
    }
}
