package com.myhome.server.component.batch.cloudPublic;

import com.myhome.server.component.LogComponent;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CloudPublicFailedTasklet implements Tasklet {
    private final static String TOPIC_CLOUD_CHECK_LOG = "cloud-check-log";
    @Autowired
    private LogComponent logComponent;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logComponent.sendLog("Cloud-Check", "cloud public fileswalk was failed", false, TOPIC_CLOUD_CHECK_LOG);
        return RepeatStatus.FINISHED;
    }
}
