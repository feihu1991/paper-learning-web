package com.paperlearning.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArxivSearchResult {
    private String arxivId;
    private String title;
    private String authors;
    private String abstract_;
    private String published;
    private String pdfUrl;

    public String getAuthorsList() {
        return authors;
    }
}
