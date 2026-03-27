package com.paperlearning.controller;

import com.paperlearning.dto.ApiResponse;
import com.paperlearning.entity.LearningStep;
import com.paperlearning.entity.UserProgress;
import com.paperlearning.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class LearningController {

    private final LearningPathService learningPathService;

    @GetMapping("/papers/{paperId}/steps")
    public ResponseEntity<ApiResponse<List<LearningStep>>> getStepsByPaperId(@PathVariable Long paperId) {
        List<LearningStep> steps = learningPathService.getStepsByPaperId(paperId);
        return ResponseEntity.ok(ApiResponse.success(steps));
    }

    @GetMapping("/papers/{paperId}/progress")
    public ResponseEntity<ApiResponse<UserProgress>> getProgress(@PathVariable Long paperId) {
        UserProgress progress = learningPathService.getOrCreateProgress(paperId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @PostMapping("/papers/{paperId}/steps/{stepId}/complete")
    public ResponseEntity<ApiResponse<UserProgress>> completeStep(
            @PathVariable Long paperId,
            @PathVariable Long stepId) {
        UserProgress progress = learningPathService.completeStep(paperId, stepId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @PostMapping("/papers/{paperId}/steps/{stepId}/uncomplete")
    public ResponseEntity<ApiResponse<UserProgress>> uncompleteStep(
            @PathVariable Long paperId,
            @PathVariable Long stepId) {
        UserProgress progress = learningPathService.uncompleteStep(paperId, stepId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }

    @PostMapping("/papers/{paperId}/reset")
    public ResponseEntity<ApiResponse<UserProgress>> resetProgress(@PathVariable Long paperId) {
        UserProgress progress = learningPathService.resetProgress(paperId);
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
}
