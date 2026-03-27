package com.paperlearning.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlearning.entity.LearningStep;
import com.paperlearning.entity.Paper;
import com.paperlearning.entity.UserProgress;
import com.paperlearning.repository.LearningStepRepository;
import com.paperlearning.repository.PaperRepository;
import com.paperlearning.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPathService {

    private final LearningStepRepository stepRepository;
    private final PaperRepository paperRepository;
    private final UserProgressRepository progressRepository;
    private final LlmService llmService;
    private final LlmConfigService configService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public List<LearningStep> generatePath(Long paperId) {
        Paper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new RuntimeException("Paper not found: " + paperId));

        // Delete existing steps
        stepRepository.deleteByPaperId(paperId);

        // Try to use LLM to generate a structured path
        var configOpt = configService.getActiveConfig();
        String pathJson = null;

        if (configOpt.isPresent()) {
            var cfg = configOpt.get();
            String apiKey = System.getenv(cfg.getApiKeyAlias()) != null
                ? System.getenv(cfg.getApiKeyAlias())
                : cfg.getApiKeyAlias();
            if (apiKey != null) {
                pathJson = llmService.generateLearningPath(
                    paper.getTitle(),
                    paper.getAuthors(),
                    paper.getPaperAbstract(),
                    cfg.getModel(),
                    cfg.getApiEndpoint(),
                    apiKey
                );
            }
        }

        List<Map<String, Object>> steps = new ArrayList<>();

        if (pathJson != null) {
            try {
                steps = objectMapper.readValue(pathJson, new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse LLM response as JSON, using fallback: {}", e.getMessage());
            }
        }

        // Fallback steps if LLM failed or returned nothing
        if (steps.isEmpty()) {
            steps = createFallbackSteps();
        }

        List<LearningStep> savedSteps = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            Map<String, Object> stepData = steps.get(i);
            LearningStep step = LearningStep.builder()
                    .paper(paper)
                    .stepOrder(i)
                    .stepType(parseStepType((String) stepData.get("type")))
                    .title((String) stepData.get("title"))
                    .content((String) stepData.get("content"))
                    .estimatedMinutes(parseIntOrDefault(stepData.get("estimated_minutes"), 5))
                    .build();
            savedSteps.add(stepRepository.save(step));
        }

        // Initialize user progress
        UserProgress progress = UserProgress.builder()
                .paper(paper)
                .currentStep(0)
                .completedSteps("[]")
                .build();
        progressRepository.save(progress);

        return savedSteps;
    }

    public List<LearningStep> getSteps(Long paperId) {
        return stepRepository.findByPaperIdOrderByStepOrderAsc(paperId);
    }

    public Optional<UserProgress> getProgress(Long paperId) {
        return progressRepository.findByPaperId(paperId);
    }

    @Transactional
    public void updateProgress(Long paperId, int currentStep, List<Integer> completedSteps) {
        UserProgress progress = progressRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        progress.setCurrentStep(currentStep);
        progress.setCompletedSteps(objectMapper.writeValueAsString(completedSteps));
        progressRepository.save(progress);
    }

    @Transactional
    public void markCompleted(Long paperId) {
        UserProgress progress = progressRepository.findByPaperId(paperId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        progress.setCompletedAt(java.time.LocalDateTime.now());
        progressRepository.save(progress);
    }

    private List<Map<String, Object>> createFallbackSteps() {
        return List.of(
            Map.of("title","研究背景","type","BACKGROUND","content","理解论文所在领域的研究背景和动机","estimated_minutes",5),
            Map.of("title","核心方法","type","METHOD","content","详细阅读并理解论文提出的主要方法和技术","estimated_minutes",15),
            Map.of("title","实验分析","type","EXPERIMENT","content","分析论文的实验设计、结果和有效性验证","estimated_minutes",10),
            Map.of("title","创新贡献","type","CONCLUSION","content","总结论文的关键创新点和主要贡献","estimated_minutes",5),
            Map.of("title","局限性","type","DISCUSSION","content","讨论论文的局限性以及可能的改进方向","estimated_minutes",5)
        );
    }

    private LearningStep.StepType parseStepType(String type) {
        if (type == null) return LearningStep.StepType.BACKGROUND;
        return switch (type.toUpperCase()) {
            case "METHOD" -> LearningStep.StepType.METHOD;
            case "EXPERIMENT" -> LearningStep.StepType.EXPERIMENT;
            case "CONCLUSION" -> LearningStep.StepType.CONCLUSION;
            case "DISCUSSION" -> LearningStep.StepType.DISCUSSION;
            default -> LearningStep.StepType.BACKGROUND;
        };
    }

    private int parseIntOrDefault(Object value, int defaultVal) {
        if (value == null) return defaultVal;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (Exception e) { return defaultVal; }
    }
}
