package com.jobs.job.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    private String jobId; // LinkedIn / Indeed ID

    private String title;
    private String company;
    private String location;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private String source;
}

