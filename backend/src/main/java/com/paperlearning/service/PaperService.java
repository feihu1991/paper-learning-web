package com.paperlearning.service;

import com.paperlearning.dto.PaperDTO;
import com.paperlearning.entity.Paper;
import com.paperlearning.repository.PaperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaperService {

    private final PaperRepository paperRepository;

    public Page<PaperDTO> getAllPapers(Pageable pageable) {
        return paperRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(PaperDTO::fromEntity);
    }

    public Optional<PaperDTO> getPaperById(Long id) {
        return paperRepository.findById(id).map(PaperDTO::fromEntity);
    }

    public Page<PaperDTO> searchPapers(String query, Pageable pageable) {
        return paperRepository.search(query, pageable).map(PaperDTO::fromEntity);
    }

    @Transactional
    public PaperDTO savePaper(Paper paper) {
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
}
