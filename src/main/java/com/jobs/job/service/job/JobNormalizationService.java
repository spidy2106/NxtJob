package com.jobs.job.service.job;


import com.jobs.job.ai.OpenAiClient;
import com.jobs.job.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobNormalizationService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Job normalize(String rawContent, String source) {

        try {
            String prompt = buildPrompt(rawContent);
            String aiResponse = openAiClient.call(prompt);

            log.info("AI normalization response: {}", aiResponse);

            JsonNode json = objectMapper.readTree(aiResponse);

            Job job = Job.builder()
                    .jobId(String.valueOf(rawContent.hashCode()))
                    .title(json.path("title").asText("Unknown Role"))
                    .company(json.path("company").asText("Unknown Company"))
                    .location(json.path("location").asText("Unknown Location"))
                    .description(json.path("description").asText(rawContent))
                    .source(source)
                    .build();

            log.info(
                    "Normalized job via AI title={} company={} location={}",
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation()
            );

            return job;

        } catch (Exception e) {
            log.warn("AI normalization failed, falling back", e);
            return fallback(rawContent, source);
        }
    }

    private String buildPrompt(String rawContent) {
        return """
        You are a system that extracts structured job information.

        Return ONLY valid JSON.
        Do NOT add explanation text.

        Required JSON format:
        {
          "title": "",
          "company": "",
          "location": "",
          "description": ""
        }

        Job text:
        %s
        """.formatted(rawContent);
    }

    private Job fallback(String rawContent, String source) {
        log.info("Using fallback job normalization");

        return Job.builder()
                .jobId(String.valueOf(rawContent.hashCode()))
                .title("Unknown Role")
                .company("Unknown Company")
                .location("Unknown Location")
                .description(rawContent)
                .source(source)
                .build();
    }
}
