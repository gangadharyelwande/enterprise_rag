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
   // private final SimilaritySearchService searchService;

    public ChatController(SimilaritySearchService searchService) {
        this.searchService = searchService;
      //  this.searchService = searchService;
    }

    // Question → Query Embedding → Vector Search → Top-K Chunks
    @GetMapping("/document/search")
    public ResponseEntity<List<Document>> search(
            @RequestParam String message) {

        List<Document> results =
                searchService.search(message);

        return ResponseEntity.ok(results);
    }

    /*
     * Metadata-filtered search
     *
     * Example:
     *
     * /api/search/filtered
     * ?question=What is the leave policy?
     * &category=HR
     * &department=HUMAN_RESOURCES
     * &documentType=HR_POLICY
     * &topK=3
     */
    @GetMapping("/search1/filtered")
    public List<Document> filteredSearch( @RequestParam String question,
            @RequestParam(required = false)String category,
            @RequestParam(required = false)String department,
            @RequestParam(required = false)String documentType,
            @RequestParam(defaultValue = "3")int topK) {

        return searchService.search(
                question,
                category,
                department,
                documentType,
                topK
        );
    }
}