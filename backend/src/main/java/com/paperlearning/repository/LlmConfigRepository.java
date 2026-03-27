package com.paperlearning.repository;

import com.paperlearning.entity.LlmConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LlmConfigRepository extends JpaRepository<LlmConfig, Long> {
    Optional<LlmConfig> findByActiveTrue();
    List<LlmConfig> findByPresetTrue();
}
