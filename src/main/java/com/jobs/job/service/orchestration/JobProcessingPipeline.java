package com.jobs.job.service.orchestration;

import com.jobs.job.entity.Job;
import com.jobs.job.entity.User;
import com.jobs.job.entity.UserPreference;
import com.jobs.job.repository.JobRepository;
import com.jobs.job.repository.UserRepository;
import com.jobs.job.repository.UserPreferenceRepository;
import com.jobs.job.ai.JobMatchAiService;
import com.jobs.job.service.email.EmailReaderService;
import com.jobs.job.service.job.JobNormalizationService;
import com.jobs.job.service.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobProcessingPipeline {

    private final EmailReaderService emailReader;
    private final JobNormalizationService normalizer;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final EmailNotificationService notificationService;
    private final JobMatchAiService aiService;

    public void process() {

        log.info("Job pipeline started");

        var users = userRepository.findAll();
        log.info("Active users count: {}", users.size());

        emailReader.readLinkedInJobEmails().forEach(rawEmail -> {

            log.info("Processing LinkedIn email");

            Job job = normalizer.normalize(rawEmail, "LINKEDIN_EMAIL");
            log.info("Extracted jobId={}", job.getJobId());

            if (jobRepository.existsById(job.getJobId())) {
                log.info("Job already exists, skipping jobId={}", job.getJobId());
                return;
            }

            jobRepository.save(job);
            log.info("Job saved, jobId={}", job.getJobId());

            users.stream()
                    .filter(User::getActive)
                    .forEach(user -> {

                        preferenceRepository.findByUser_Id(user.getId())
                                .ifPresent(pref -> {

                                    int score = aiService.matchScore(job, pref);

                                    log.info(
                                            "Match score user={} jobId={} score={}",
                                            user.getEmail(),
                                            job.getJobId(),
                                            score
                                    );

                                    if (score < pref.getMinMatchScore()) {
                                        log.debug(
                                                "Skipping email for user={} (score {} < {})",
                                                user.getEmail(),
                                                score,
                                                pref.getMinMatchScore()
                                        );
                                        return;
                                    }

                                    log.info("Sending job alert to {}", user.getEmail());

                                    notificationService.sendJobAlert(
                                            user.getEmail(),
                                            job.getTitle(),
                                            job.getCompany(),
                                            job.getSource()
                                    );
                                });
                    });
        });

        log.info("Job pipeline finished");
    }
}
