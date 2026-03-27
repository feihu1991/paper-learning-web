package com.paperlearning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column(name = "current_step")
    private Integer currentStep = 0;

    @Column(name = "completed_steps", columnDefinition = "TEXT")
    private String completedSteps = "[]";

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_mode")
    private LearningMode learningMode = LearningMode.STANDARD;

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum LearningMode {
        STANDARD, QUICK, DETAILED
    }
}
