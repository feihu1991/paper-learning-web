package com.paperlearning.dto;

import com.paperlearning.entity.Paper;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaperDTO {
    private Long id;
    private String arxivId;
    private String title;
    private String authors;
    private String paperAbstract;
    private String pdfPath;
    private String sourceUrl;
    private Paper.ParsedStatus parsedStatus;
    private String structuredSummary;
    private LocalDateTime createdAt;

    public static PaperDTO fromEntity(Paper paper) {
        return PaperDTO.builder()
                .id(paper.getId())
                .arxivId(paper.getArxivId())
                .title(paper.getTitle())
                .authors(paper.getAuthors())
                .paperAbstract(paper.getPaperAbstract())
                .pdfPath(paper.getPdfPath())
                .sourceUrl(paper.getSourceUrl())
                .parsedStatus(paper.getParsedStatus())
                .structuredSummary(paper.getStructuredSummary())
                .createdAt(paper.getCreatedAt())
                .build();
    }
}
