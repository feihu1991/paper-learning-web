package com.paperlearning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "llm_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "api_endpoint", nullable = false)
    private String apiEndpoint;

    @Column(nullable = false)
    private String model;

    // API key is stored encrypted or via environment variable reference
    @Column(name = "api_key_alias")
    private String apiKeyAlias;

    private Boolean active = false;

    private Boolean preset = false;
}
