package com.jobs.job.repository;

import com.jobs.job.entity.EmailCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCheckpointRepository
        extends JpaRepository<EmailCheckpoint, String> {
}