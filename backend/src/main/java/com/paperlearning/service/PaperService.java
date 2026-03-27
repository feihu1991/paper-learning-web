package com.paperlearning.service;

import com.paperlearning.dto.ArxivSearchResult;
import com.paperlearning.dto.PaperDTO;
import com.paperlearning.entity.Paper;
import com.paperlearning.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaperService {

    private final PaperRepository paperRepository;
    private final ArxivService arxivService;
    private final LlmService llmService;
    private final LlmConfigService llmConfigService;

    public java.util.List<PaperDTO> getAllPapers() {
        return getAllPapers(PageRequest.of(0, 50));
    }

    public Page<PaperDTO> getAllPapers(Pageable pageable) {
        return paperRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(PaperDTO::fromEntity);
    }

    public Optional<PaperDTO> getPaperById(Long id) {
        return paperRepository.findById(id).map(PaperDTO::fromEntity);
    }

    public java.util.List<PaperDTO> searchPapers(String keyword) {
        return searchPapers(keyword, PageRequest.of(0, 50));
    }

    public Page<PaperDTO> searchPapers(String query, Pageable pageable) {
        return paperRepository.search(query, pageable).map(PaperDTO::fromEntity);
    }

    @Transactional
    public PaperDTO savePaper(PaperDTO dto) {
        Paper paper = Paper.builder()
                .title(dto.getTitle())
                .authors(dto.getAuthors())
                .paperAbstract(dto.getPaperAbstract())
                .pdfPath(dto.getPdfPath())
                .sourceUrl(dto.getSourceUrl())
                .arxivId(dto.getArxivId())
                .parsedStatus(Paper.ParsedStatus.NOT_PARSED)
                .build();
        Paper saved = paperRepository.save(paper);
        log.info("Paper saved: id={}, title={}", saved.getId(), saved.getTitle());
        return PaperDTO.fromEntity(saved);
    }

    @Transactional
    public PaperDTO updatePaper(Long id, Paper paper) {
        Paper existing = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found: " + id));
        existing.setTitle(paper.getTitle());
        existing.setAuthors(paper.getAuthors());
        existing.setPaperAbstract(paper.getPaperAbstract());
        existing.setPdfPath(paper.getPdfPath());
        existing.setParsedStatus(paper.getParsedStatus());
        existing.setStructuredSummary(paper.getStructuredSummary());
        return PaperDTO.fromEntity(paperRepository.save(existing));
    }

    @Transactional
    public void deletePaper(Long id) {
        paperRepository.deleteById(id);
        log.info("Paper deleted: id={}", id);
    }

    @Transactional
    public PaperDTO updateParseStatus(Long id, Paper.ParsedStatus status) {
        Paper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found: " + id));
        paper.setParsedStatus(status);
        return PaperDTO.fromEntity(paperRepository.save(paper));
    }

    @Transactional
    public PaperDTO importFromArxiv(ArxivSearchResult arxivResult) {
        Paper paper = Paper.builder()
                .arxivId(arxivResult.getArxivId())
                .title(arxivResult.getTitle())
                .authors(arxivResult.getAuthors())
                .paperAbstract(arxivResult.getAbstract_())
                .sourceUrl(arxivResult.getPdfUrl().replace("/pdf", "/abs"))
                .pdfPath(arxivResult.getPdfUrl())
                .parsedStatus(Paper.ParsedStatus.NOT_PARSED)
                .build();
        Paper saved = paperRepository.save(paper);
        log.info("Paper imported from ArXiv: id={}, arxivId={}", saved.getId(), saved.getArxivId());
        return PaperDTO.fromEntity(saved);
    }

    public PaperDTO parsePaperWithLlm(Long id) {
        Optional<Paper> optPaper = paperRepository.findById(id);
        if (optPaper.isEmpty()) {
            log.error("Paper not found: {}", id);
            return null;
        }
        Paper paper = optPaper.get();
        var configOpt = llmConfigService.getActiveConfig();
        if (configOpt.isEmpty()) {
            log.error("No active LLM config");
            return null;
        }
        var config = configOpt.get();
        String apiKey = System.getenv(config.getApiKeyAlias());
        if (apiKey == null) apiKey = config.getApiKeyAlias();
        String summary = llmService.parsePaper(
                paper.getTitle(),
                paper.getAuthors(),
                paper.getPaperAbstract(),
                config.getModel(),
                config.getApiEndpoint(),
                apiKey
        );
        if (summary != null) {
            paper.setStructuredSummary(summary);
            paper.setParsedStatus(Paper.ParsedStatus.COMPLETED);
        } else {
            paper.setParsedStatus(Paper.ParsedStatus.FAILED);
        }
        return PaperDTO.fromEntity(paperRepository.save(paper));
    }
}
