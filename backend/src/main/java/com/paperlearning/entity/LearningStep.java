package com.paperlearning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learning_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type")
    private StepType stepType = StepType.BACKGROUND;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "media_path")
    private String mediaPath;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes = 5;

    public enum StepType {
        BACKGROUND, METHOD, EXPERIMENT, CONCLUSION, DISCUSSION
    }
}
