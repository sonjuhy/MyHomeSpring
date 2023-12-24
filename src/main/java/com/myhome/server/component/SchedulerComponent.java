package com.myhome.server.component;

import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.FileServerPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerComponent {

    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    @Autowired
    LogComponent logComponent;

    @Autowired
    FileServerPrivateService fileServerPrivateService;

    @Autowired
    FileServerPublicService fileServerPublicService;

    @Scheduled(cron = "0 0 0 * * *") // top of e every day
    public void checkCloudFile() {
//        try {
//            fileServerPublicService.publicFileStateCheck();
//            logComponent.sendLog("cloud-Check", "[checkCloudFile(public)] check success", true, TOPIC_CLOUD_CHECK_LOG);
//        }
//        catch (Exception e){
//            logComponent.sendErrorLog("Cloud-Check", "[checkCloudFile(private)] error : ", e, TOPIC_CLOUD_CHECK_LOG);
//        }
//        try {
//            fileServerPrivateService.privateFileCheck();
//            logComponent.sendLog("Cloud-Check", "[checkCloudFile(private)] check success", true, TOPIC_CLOUD_CHECK_LOG);
//
//        }
//        catch (Exception e){
//            logComponent.sendErrorLog("Cloud-Check", "[checkCloudFile(private)] error : ", e, TOPIC_CLOUD_CHECK_LOG);
//        }
    }
}
