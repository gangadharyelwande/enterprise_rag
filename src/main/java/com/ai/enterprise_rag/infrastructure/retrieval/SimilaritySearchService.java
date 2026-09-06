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

    private final boolean rerankingEnabled = true;
    private final int candidateTopK = 10;
    private final int finalTopK = 3;

    public SimilaritySearchService(
            QueryRewriteService queryRewriteService,
            VectorStore vectorStore,
            RerankingService rerankingService,
            KeywordSearchService keywordSearchService,
            VectorStoreService vectorStoreService, ContextCompressionService contextCompressionService) {

        this.queryRewriteService = queryRewriteService;
        this.vectorStore = vectorStore;
        this.rerankingService = rerankingService;
        this.keywordSearchService = keywordSearchService;
        this.vectorStoreService = vectorStoreService;
        this.contextCompressionService = contextCompressionService;
    }

    public List<Document> search(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }

        logger.info("========== RETRIEVAL START ==========");
        logger.info("QUESTION: {}", question);

        // 1. Rewrite the question into a retrieval-friendly query.
        String query = queryRewriteService.rewrite(question);
        logger.info("QUERY REWRITE: {}", query);

        // 2. Semantic/vector search finds documents by meaning.
        List<Document> vectorResults = vectorSearch(query, candidateTopK);
        logDocuments("VECTOR SEARCH", vectorResults);

        // 3. Keyword search finds documents containing exact terms.
        List<Document> keywordResults = keywordSearchService.search(
                query,
                vectorStoreService.getDocuments(),
                candidateTopK
        );
        logDocuments("KEYWORD SEARCH", keywordResults);

        // 4. Combine both result sets and remove duplicate documents.
        List<Document> hybridCandidates =
                combineResults(vectorResults, keywordResults);

        logger.info("COMBINE + DEDUPLICATE: {} candidates",
                hybridCandidates.size());
        logDocuments("HYBRID CANDIDATES", hybridCandidates);

        List<Document> finalDocuments;

        // 5. Reranker reorders candidates based on relevance.
        if (rerankingEnabled) {
            finalDocuments = rerankingService.rerank(
                    query,
                    hybridCandidates,
                    finalTopK
            );

            logDocuments("RERANKED", finalDocuments);

            // Remove irrelevant content from the selected chunks.
            finalDocuments = contextCompressionService.compress(
                    query,
                    finalDocuments
            );

            logDocuments("AFTER COMPRESSION", finalDocuments);

        } else {
            finalDocuments = hybridCandidates.stream()
                    .limit(finalTopK)
                    .toList();

            logDocuments("FINAL WITHOUT RERANKING", finalDocuments);
        }

        // 6. Only the final Top-K chunks continue to the next RAG stage.
        logger.info("FINAL TOP {}: {} documents", finalTopK, finalDocuments.size());
        logger.info("========== RETRIEVAL END ==========");

        return finalDocuments;
    }

    private List<Document> vectorSearch(String query, int topK) {

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(0.75)
                .build();

        return vectorStore.similaritySearch(request);
    }

    private List<Document> combineResults(
            List<Document> vectorResults,
            List<Document> keywordResults) {

        Map<String, Document> uniqueDocuments = new LinkedHashMap<>();

        // Vector results are added first.
        vectorResults.forEach(d -> uniqueDocuments.put(d.getId(), d));

        // Keyword results are added only if not already present.
        keywordResults.forEach(d ->
                uniqueDocuments.putIfAbsent(d.getId(), d));

        return new ArrayList<>(uniqueDocuments.values());
    }

    private void logDocuments(String stage, List<Document> documents) {

        logger.info("----- {} -----", stage);

        for (int i = 0; i < documents.size(); i++) {
            Document d = documents.get(i);

            logger.info(
                    "#{} | id={} | vector={} | keyword={} | rerank={}",
                    i + 1,
                    d.getId(),
                    d.getScore(),
                    d.getMetadata().get("keywordScore"),
                    d.getMetadata().get("rerankScore")
            );
        }
    }
}