package com.paperlearning.service;

import com.paperlearning.entity.LlmConfig;
import com.paperlearning.repository.LlmConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmConfigService {

    private final LlmConfigRepository llmConfigRepository;

    public List<LlmConfig> getAllConfigs() {
        return llmConfigRepository.findAll();
    }

    public Optional<LlmConfig> getActiveConfig() {
        return llmConfigRepository.findByActiveTrue();
    }

    public Optional<LlmConfig> getConfigById(Long id) {
        return llmConfigRepository.findById(id);
    }

    @Transactional
    public LlmConfig createConfig(LlmConfig config) {
        if (Boolean.TRUE.equals(config.getActive())) {
            deactivateAllConfigs();
        }
        return llmConfigRepository.save(config);
    }

    @Transactional
    public LlmConfig updateConfig(Long id, LlmConfig config) {
        LlmConfig existing = llmConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LLM Config not found: " + id));

        existing.setName(config.getName());
        existing.setApiEndpoint(config.getApiEndpoint());
        existing.setModel(config.getModel());
        existing.setApiKeyAlias(config.getApiKeyAlias());
        existing.setActive(config.getActive());
        existing.setPreset(config.getPreset());

        if (Boolean.TRUE.equals(config.getActive())) {
            deactivateAllConfigs();
        }

        return llmConfigRepository.save(existing);
    }

    @Transactional
    public void deleteConfig(Long id) {
        llmConfigRepository.deleteById(id);
    }

    @Transactional
    public LlmConfig setActive(Long id) {
        deactivateAllConfigs();
        LlmConfig config = llmConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("LLM Config not found: " + id));
        config.setActive(true);
        return llmConfigRepository.save(config);
    }

    private void deactivateAllConfigs() {
        llmConfigRepository.findByActiveTrue().ifPresent(config -> {
            config.setActive(false);
            llmConfigRepository.save(config);
        });
    }

    public boolean hasActiveConfig() {
        return llmConfigRepository.findByActiveTrue().isPresent();
    }
}
