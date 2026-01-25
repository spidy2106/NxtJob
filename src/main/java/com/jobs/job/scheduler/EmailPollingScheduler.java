package com.jobs.job.scheduler;

import com.jobs.job.service.orchestration.JobProcessingPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailPollingScheduler {

    private final JobProcessingPipeline pipeline;

    // every 1 minute
    @Scheduled(fixedDelay = 300000) // 5 minutes
    public void pollEmails() {
        try {
            log.info("Scheduler tick");
            pipeline.process();
        } catch (Exception e) {
            log.error("Scheduler error", e);
        }
    }

}

