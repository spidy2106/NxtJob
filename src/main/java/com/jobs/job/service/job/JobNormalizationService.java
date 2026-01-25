package com.jobs.job.service.job;

import com.jobs.job.entity.Job;
import org.springframework.stereotype.Service;

@Service
public class JobNormalizationService {

    public Job normalize(String rawContent, String source) {

        // TEMP SIMPLE VERSION
        return Job.builder()
                .jobId(extractJobId(rawContent))
                .title(extractTitle(rawContent))
                .company(extractCompany(rawContent))
                .description(rawContent)
                .source(source)
                .build();
    }

    private String extractJobId(String text) {
        return String.valueOf(text.hashCode());
    }

    private String extractTitle(String text) {
        return "Java Developer";
    }

    private String extractCompany(String text) {
        return "Unknown Company";
    }
}
