package com.paperlearning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlearning.entity.LlmConfig;
import com.paperlearning.repository.LlmConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmService {

    private final LlmConfigRepository llmConfigRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<LlmConfig> getActiveConfig() {
        return llmConfigRepository.findByActiveTrue();
    }

    public String parsePaper(String title, String authors, String abstractText, String model, String apiEndpoint, String apiKey) {
        String systemPrompt = """
            You are a professional academic paper analysis assistant. Analyze the given paper and provide a structured summary in the following format:

            ## 研究背景
            [Brief background and significance of the research field]

            ## 核心问题
            [The core problem the paper addresses]

            ## 主要方法
            [The main methods or techniques proposed]

            ## 关键创新
            [Key innovations and contributions]

            ## 实验结果
            [Main experimental results and findings]

            ## 局限性
            [Limitations and future work]

            Keep responses concise and accurate. Respond in Chinese.
            """;

        String userPrompt = String.format("""
            Please analyze the following paper and generate a structured summary:

            Title: %s
            Authors: %s
            Abstract: %s

            Follow the format above.
            """, title, authors, abstractText);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        request.put("temperature", 0.7);
        request.put("max_tokens", 1500);

        try {
            String url = normalizeUrl(apiEndpoint) + "chat/completions";
            String apiKeyHeader = apiKey.startsWith("Bearer ") ? apiKey : "Bearer " + apiKey;

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKeyHeader);

            var entity = new org.springframework.http.HttpEntity<>(request, headers);
            var response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }
            }
        } catch (Exception e) {
            log.error("LLM API call failed", e);
        }
        return null;
    }

    public String generateLearningPath(String title, String authors, String abstractText, String model, String apiEndpoint, String apiKey) {
        String systemPrompt = """
            You are an expert academic advisor. Create a structured learning path for studying this academic paper.

            Based on the paper's content, create 5-8 learning steps covering:
            1. Background knowledge required
            2. Paper's main method/approach
            3. Experimental setup and results
            4. Key insights and contributions
            5. Limitations and potential improvements
            6. Practical applications
            7. How to replicate or extend the work

            For each step provide:
            - title (brief, max 20 chars)
            - type: BACKGROUND | METHOD | EXPERIMENT | CONCLUSION | DISCUSSION
            - content: detailed explanation (2-3 sentences)
            - estimated_minutes: reasonable time estimate

            Respond ONLY with a JSON array, no other text.
            Example format:
            [{"title":"标题","type":"BACKGROUND","content":"内容...","estimated_minutes":5},...]
            """;

        String userPrompt = String.format("Title: %s\nAuthors: %s\nAbstract: %s", title, authors, abstractText);

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", model);
        request.put("messages", List.of(
            Map.of("role", "system", "content", systemPrompt),
            Map.of("role", "user", "content", userPrompt)
        ));
        request.put("temperature", 0.7);
        request.put("max_tokens", 2000);

        try {
            String url = normalizeUrl(apiEndpoint) + "chat/completions";
            String apiKeyHeader = apiKey.startsWith("Bearer ") ? apiKey : "Bearer " + apiKey;

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.set("Authorization", apiKeyHeader);

            var entity = new org.springframework.http.HttpEntity<>(request, headers);
            var response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    return choices.get(0).get("message").get("content").asText();
                }
            }
        } catch (Exception e) {
            log.error("LLM learning path generation failed", e);
        }
        return null;
    }

    private String normalizeUrl(String url) {
        if (url == null) return "https://api.openai.com/v1/";
        return url.endsWith("/") ? url : url + "/";
    }
}
