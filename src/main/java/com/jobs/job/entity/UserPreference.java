package com.jobs.job.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Proper relation to User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String preferredRoles;
    private String preferredLocations;
    private String techStack;

    private int minMatchScore;
    private int maxAlertsPerDay;

    private boolean remoteOnly;
}
