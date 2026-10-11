package com.ai.enterprise_rag.infrastructure.retrieval;

import com.ai.enterprise_rag.infrastructure.ingestion.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimilaritySearchService {

    private static final Logger logger =
            LoggerFactory.getLogger(SimilaritySearchService.class);

    private final VectorStore vectorStore;
    private final VectorStoreService vectorStoreService;
    private final QueryRewriteService queryRewriteService;
    private final KeywordSearchService keywordSearchService;
    private final RerankingService rerankingService;
    private final ContextCompressionService contextCompressionService;

    private final int candidateTopK = 10;
    private final int finalTopK = 3;

    public SimilaritySearchService(
            QueryRewriteService queryRewriteService,
            VectorStore vectorStore,
            RerankingService rerankingService,
            KeywordSearchService keywordSearchService,
            VectorStoreService vectorStoreService,
            ContextCompressionService contextCompressionService) {

        this.queryRewriteService = queryRewriteService;
        this.vectorStore = vectorStore;
        this.rerankingService = rerankingService;
        this.keywordSearchService = keywordSearchService;
        this.vectorStoreService = vectorStoreService;
        this.contextCompressionService = contextCompressionService;
    }

    /**
     * Existing public search method.
     *
     * This method performs query rewriting internally.
     */
    public List<Document> search(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be blank"
            );
        }

        String query =
                queryRewriteService.rewrite(question);

        return searchRewrittenQuery(query);
    }

    /**
     * Performs retrieval using a query that has already
     * been rewritten by the caller.
     *
     * This prevents QueryRewriteService from being called twice
     * when used inside RagWorkflow.
     */
    public List<Document> searchRewrittenQuery(String query) {

        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException(
                    "Retrieval query must not be blank"
            );
        }

        logger.info("========== RETRIEVAL START ==========");
        logger.info("RETRIEVAL QUERY: {}", query);

        // -------------------------------------------------
        // 1. Semantic / vector search
        // -------------------------------------------------

        List<Document> vectorResults =
                vectorSearch(
                        query,
                        candidateTopK
                );

        logDocuments(
                "VECTOR SEARCH",
                vectorResults
        );

        // -------------------------------------------------
        // 2. Keyword search
        // -------------------------------------------------

        List<Document> keywordResults =
                keywordSearchService.search(
                        query,
                        vectorStoreService.getDocuments(),
                        candidateTopK
                );

        logDocuments(
                "KEYWORD SEARCH",
                keywordResults
        );

        // -------------------------------------------------
        // 3. Hybrid merge + deduplication
        // -------------------------------------------------

        List<Document> hybridCandidates =
                combineResults(
                        vectorResults,
                        keywordResults
                );

        logger.info(
                "COMBINE + DEDUPLICATE: {} candidates",
                hybridCandidates.size()
        );

        logDocuments(
                "HYBRID CANDIDATES",
                hybridCandidates
        );

        // -------------------------------------------------
        // 4. Reranking
        // -------------------------------------------------

        List<Document> rerankedDocuments =
                rerankingService.rerank(
                        query,
                        hybridCandidates,
                        finalTopK
                );

        logDocuments(
                "RERANKED",
                rerankedDocuments
        );

        // -------------------------------------------------
        // 5. Context compression
        // -------------------------------------------------

        List<Document> finalDocuments =
                contextCompressionService.compress(
                        query,
                        rerankedDocuments
                );

        logDocuments(
                "AFTER COMPRESSION",
                finalDocuments
        );

        // -------------------------------------------------
        // 6. Final Top-K documents
        // -------------------------------------------------

        logger.info(
                "FINAL TOP {}: {} documents",
                finalTopK,
                finalDocuments.size()
        );

        logger.info("========== RETRIEVAL END ==========");

        return finalDocuments;
    }

    private List<Document> vectorSearch(
            String query,
            int topK) {

        SearchRequest request =
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .similarityThreshold(0.75)
                        .build();

        return vectorStore.similaritySearch(request);
    }

    private List<Document> combineResults(
            List<Document> vectorResults,
            List<Document> keywordResults) {

        Map<String, Document> uniqueDocuments =
                new LinkedHashMap<>();

        // Vector results first
        vectorResults.forEach(
                document ->
                        uniqueDocuments.put(
                                document.getId(),
                                document
                        )
        );

        // Add keyword results only when not already present
        keywordResults.forEach(
                document ->
                        uniqueDocuments.putIfAbsent(
                                document.getId(),
                                document
                        )
        );

        return new ArrayList<>(
                uniqueDocuments.values()
        );
    }

    private void logDocuments(
            String stage,
            List<Document> documents) {

        logger.info(
                "----- {} -----",
                stage
        );

        for (int i = 0;
             i < documents.size();
             i++) {

            Document document =
                    documents.get(i);

            logger.info(
                    "#{} | id={} | vector={} | keyword={} | rerank={}",
                    i + 1,
                    document.getId(),
                    document.getScore(),
                    document.getMetadata()
                            .get("keywordScore"),
                    document.getMetadata()
                            .get("rerankScore")
            );
        }
    }
}
