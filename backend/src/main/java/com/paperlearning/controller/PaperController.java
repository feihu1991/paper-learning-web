package com.paperlearning.controller;

import com.paperlearning.dto.ApiResponse;
import com.paperlearning.dto.ArxivSearchResult;
import com.paperlearning.dto.PaperDTO;
import com.paperlearning.entity.Paper;
import com.paperlearning.entity.LlmConfig;
import com.paperlearning.entity.Paper;
import com.paperlearning.service.ArxivService;
import com.paperlearning.service.LlmService;
import com.paperlearning.service.LlmConfigService;
import com.paperlearning.service.PaperService;
import com.paperlearning.service.LearningPathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/papers")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PaperController {

    private final PaperService paperService;
    private final ArxivService arxivService;
    private final LlmService llmService;
    private final LlmConfigService llmConfigService;
    private final LearningPathService learningPathService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaperDTO>>> getAllPapers() {
        List<PaperDTO> papers = paperService.getAllPapers();
        return ResponseEntity.ok(ApiResponse.success(papers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaperDTO>> getPaperById(@PathVariable Long id) {
        return paperService.getPaperById(id)
                .map(paper -> ResponseEntity.ok(ApiResponse.success(paper)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PaperDTO>>> searchPapers(@RequestParam String keyword) {
        List<PaperDTO> papers = paperService.searchPapers(keyword);
        return ResponseEntity.ok(ApiResponse.success(papers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaperDTO>> savePaper(@RequestBody PaperDTO paperDTO) {
        PaperDTO saved = paperService.savePaper(paperDTO);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePaper(@PathVariable Long id) {
        paperService.deletePaper(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/arxiv/search")
    public ResponseEntity<ApiResponse<List<ArxivSearchResult>>> searchArxiv(@RequestParam String query) {
        List<ArxivSearchResult> results = arxivService.searchArxiv(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/arxiv/import")
    public ResponseEntity<ApiResponse<PaperDTO>> importFromArxiv(@RequestBody ArxivSearchResult arxivResult) {
        PaperDTO imported = paperService.importFromArxiv(arxivResult);
        return ResponseEntity.ok(ApiResponse.success(imported));
    }

    @PostMapping("/{id}/parse")
    public ResponseEntity<ApiResponse<PaperDTO>> parsePaper(@PathVariable Long id) {
        if (!llmConfigService.hasActiveConfig()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No active LLM configuration. Please configure LLM settings first."));
        }

        PaperDTO parsed = paperService.parsePaperWithLlm(id);
        if (parsed != null) {
            return ResponseEntity.ok(ApiResponse.success(parsed));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to parse paper. Check logs for details."));
        }
    }

    @PostMapping("/{id}/generate-learning-path")
    public ResponseEntity<ApiResponse<PaperDTO>> generateLearningPath(@PathVariable Long id) {
        if (!llmConfigService.hasActiveConfig()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("No active LLM configuration. Please configure LLM settings first."));
        }

        Paper updated = learningPathService.generateLearningPath(id);
        if (updated != null) {
            return ResponseEntity.ok(ApiResponse.success(PaperDTO.fromEntity(updated)));
        } else {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Failed to generate learning path."));
        }
    }
}
