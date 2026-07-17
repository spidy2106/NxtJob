package com.jobs.job.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {


    @Value("${openai.api.key}")
    private String apiKey;


    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String call(String prompt) {
        log.info("Calling OpenAI...");

        try {
            // 1. Build JSON Safely (No string concatenation!)
            ObjectNode jsonBody = objectMapper.createObjectNode();
            jsonBody.put("model", "gpt-4o-mini"); // correct model name
            jsonBody.put("temperature", 0.7);

            ArrayNode messages = jsonBody.putArray("messages");
            messages.addObject().put("role", "system").put("content", "You are a helpful job matching assistant.");
            messages.addObject().put("role", "user").put("content", prompt);

            String requestBodyString = objectMapper.writeValueAsString(jsonBody);

            // 2. Prepare Request
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(RequestBody.create(requestBodyString, MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            // 3. Execute & Handle Response
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    // This logs the ACTUAL reason (e.g., "context_length_exceeded")
                    log.error("OpenAI Failed: Code={} Body={}", response.code(), responseBody);
                    return "Error: AI Match Failed";
                }

                // 4. Extract Content
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            }

        } catch (Exception e) {
            log.error("Exception calling OpenAI", e);
            return "Error: System Failure";
        }
    }
}