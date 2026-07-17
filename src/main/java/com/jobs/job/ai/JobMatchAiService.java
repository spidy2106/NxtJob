package com.jobs.job.ai;

import com.jobs.job.entity.Job;
import com.jobs.job.entity.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobMatchAiService {

    private final OpenAiClient openAiClient;

    @Value("${openai.enabled:true}")
    private boolean enabled;

    public int matchScore(Job job, UserPreference pref) {
        if (job == null || pref == null) return 0;

        if (!enabled) {
            return fallbackScore(job, pref);
        }

        try {
            String prompt = buildPrompt(job, pref);
            String response = openAiClient.call(prompt);

            log.debug("OpenAI response: {}", response);
            return extractScore(response);

        } catch (Exception e) {
            log.warn("AI failed, using fallback scoring: {}", e.getMessage());
            return fallbackScore(job, pref);
        }
    }

    private String buildPrompt(Job job, UserPreference pref) {

        return """
        Given a job and user preference, return ONLY a number between 0 and 100.
        
        Job:
        Title: %s
        Company: %s
        Location: %s
        Description: %s
        
        User preference:
        Roles: %s
        Locations: %s
        Tech stack: %s
        Remote only: %s
        
        Output format:
        SCORE: <number>
        """
                .formatted(
                        job.getTitle() != null ? job.getTitle() : "Unknown",
                        job.getCompany() != null ? job.getCompany() : "Unknown",
                        job.getLocation() != null ? job.getLocation() : "Unknown",
                        // Truncate description to save tokens/money (optional but recommended)
                        job.getDescription() != null ? job.getDescription().substring(0, Math.min(job.getDescription().length(), 1000)) : "",
                        pref.getPreferredRoles() != null ? pref.getPreferredRoles() : "",
                        pref.getPreferredLocations() != null ? pref.getPreferredLocations() : "",
                        pref.getTechStack() != null ? pref.getTechStack() : "",
                        pref.isRemoteOnly()
                );
    }

    private int extractScore(String response) {
        try {

            Pattern pattern = Pattern.compile("SCORE:\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(response);

            if (matcher.find()) {
                return Math.min(Integer.parseInt(matcher.group(1)), 100);
            }


            Pattern numberOnly = Pattern.compile("(\\d{1,3})");
            Matcher numberMatcher = numberOnly.matcher(response);
            if (numberMatcher.find()) {
                return Math.min(Integer.parseInt(numberMatcher.group(1)), 100);
            }

        } catch (Exception e) {
            log.warn("Failed to parse AI score", e);
        }
        return 50;
    }

    private int fallbackScore(Job job, UserPreference pref) {
        int score = 0;


        if (hasText(job.getTitle()) && hasText(pref.getPreferredRoles()) &&
                job.getTitle().toLowerCase().contains(pref.getPreferredRoles().toLowerCase())) {
            score += 40;
        }

        if (hasText(job.getLocation()) && hasText(pref.getPreferredLocations()) &&
                job.getLocation().toLowerCase().contains(pref.getPreferredLocations().toLowerCase())) {
            score += 30;
        }

        if (hasText(job.getDescription()) && hasText(pref.getTechStack()) &&
                job.getDescription().toLowerCase().contains(pref.getTechStack().toLowerCase())) {
            score += 30;
        }

        return Math.min(score, 100);
    }


    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}