package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.infrastructure.retrieval.SimilaritySearchService;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final SimilaritySearchService searchService;

    public ChatController(SimilaritySearchService searchService) {
        this.searchService = searchService;
    }

    // Question → Rewrite → Vector + Keyword Search → Hybrid → Rerank → Top-K
    @GetMapping("/document/search")
    public ResponseEntity<List<Document>> search(
            @RequestParam String message) {

        List<Document> results = searchService.search(message);

        return ResponseEntity.ok(results);
    }
}