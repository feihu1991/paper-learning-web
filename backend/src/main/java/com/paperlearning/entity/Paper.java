package com.paperlearning.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "papers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arxiv_id")
    private String arxivId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String authors;

    @Column(name = "paper_abstract", columnDefinition = "TEXT")
    private String paperAbstract;

    @Column(name = "pdf_path")
    private String pdfPath;

    @Column(name = "source_url")
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "parsed_status")
    private ParsedStatus parsedStatus = ParsedStatus.NOT_PARSED;

    @Column(name = "structured_summary", columnDefinition = "TEXT")
    private String structuredSummary;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum ParsedStatus {
        NOT_PARSED, PARSING, COMPLETED, FAILED
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
