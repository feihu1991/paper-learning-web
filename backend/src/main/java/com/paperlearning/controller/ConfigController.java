package com.paperlearning.controller;

import com.paperlearning.dto.ApiResponse;
import com.paperlearning.entity.LlmConfig;
import com.paperlearning.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ConfigController {

    private final LlmConfigService llmConfigService;

    @GetMapping("/llm")
    public ResponseEntity<ApiResponse<List<LlmConfig>>> getAllConfigs() {
        List<LlmConfig> configs = llmConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/llm/active")
    public ResponseEntity<ApiResponse<LlmConfig>> getActiveConfig() {
        return llmConfigService.getActiveConfig()
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.ok(ApiResponse.success(null)));
    }

    @GetMapping("/llm/{id}")
    public ResponseEntity<ApiResponse<LlmConfig>> getConfigById(@PathVariable Long id) {
        return llmConfigService.getConfigById(id)
                .map(config -> ResponseEntity.ok(ApiResponse.success(config)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/llm")
    public ResponseEntity<ApiResponse<LlmConfig>> createConfig(@RequestBody LlmConfig config) {
        LlmConfig created = llmConfigService.createConfig(config);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PutMapping("/llm/{id}")
    public ResponseEntity<ApiResponse<LlmConfig>> updateConfig(
            @PathVariable Long id,
            @RequestBody LlmConfig config) {
        LlmConfig updated = llmConfigService.updateConfig(id, config);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/llm/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        llmConfigService.deleteConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/llm/{id}/activate")
    public ResponseEntity<ApiResponse<LlmConfig>> activateConfig(@PathVariable Long id) {
        LlmConfig activated = llmConfigService.setActive(id);
        return ResponseEntity.ok(ApiResponse.success(activated));
    }
}
