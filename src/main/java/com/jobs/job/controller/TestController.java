package com.jobs.job.controller;

import com.jobs.job.service.orchestration.JobProcessingPipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final JobProcessingPipeline pipeline;

    @PostMapping("/process")
    public ResponseEntity<String> runPipeline() {
        pipeline.process();
        return ResponseEntity.ok("Pipeline executed");
    }
}

