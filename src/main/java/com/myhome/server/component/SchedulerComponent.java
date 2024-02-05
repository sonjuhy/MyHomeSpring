package com.myhome.server.component;

import com.myhome.server.api.service.FileServerPrivateService;
import com.myhome.server.api.service.FileServerPublicService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SchedulerComponent {

    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";

    @Autowired
    LogComponent logComponent;

    @Autowired
    FileServerPrivateService fileServerPrivateService;

    @Autowired
    FileServerPublicService fileServerPublicService;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job publicCloudCheckJob;
    @Autowired
    private Job privateCloudCheckJob;

    @Scheduled(cron = "0 0 0 * * *") // top of e every day
    public void checkCloudFile() {
        logComponent.sendLog("cloud-Check", "[checkCloudFile(Reserve)] check start", true, TOPIC_CLOUD_CHECK_LOG);
        Date date = new Date();
        try {
            JobParameters publicJobParameters = new JobParametersBuilder()
                    .addString("publicCheck-" + date.getTime(), String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            jobLauncher.run(publicCloudCheckJob, publicJobParameters);
            fileServerPublicService.publicFileTrashStateCheck();
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[checkCloudFile(private)] error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
        try {
            JobParameters privateJobParameters = new JobParametersBuilder()
                    .addString("privateCheck-" + date.getTime(), String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            jobLauncher.run(publicCloudCheckJob, privateJobParameters);
            fileServerPrivateService.privateFileTrashCheck();
        }
        catch (Exception e){
            logComponent.sendErrorLog("Cloud-Check", "[checkCloudFile(private)] error : ", e, TOPIC_CLOUD_CHECK_LOG);
        }
    }
}
