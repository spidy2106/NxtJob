package com.jobs.job.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "email_checkpoint")
@Getter
@Setter
public class EmailCheckpoint {

    @Id
    private String mailbox; // e.g. LINKEDIN

    private long lastProcessedUid;
}
