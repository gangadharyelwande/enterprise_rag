package com.ai.enterprise_rag.infrastructure.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimilaritySearchService {

    private static final Logger logger =
            LoggerFactory.getLogger(SimilaritySearchService.class);

    private final VectorStore vectorStore;
    private final QueryRewriteService queryRewriteService;

    public SimilaritySearchService(QueryRewriteService queryRewriteService,
                                   VectorStore vectorStore) {
        this.queryRewriteService = queryRewriteService;
        this.vectorStore = vectorStore;
    }

    // Basic semantic search with query rewriting
    public List<Document> search(String question) {

        logger.info("========== RETRIEVAL START ==========");
        logger.info("Original question: '{}'", question);

        String rewrittenQuery = queryRewriteService.rewrite(question);

        logger.info("Retrieval query: '{}'", rewrittenQuery);

        return search(
                rewrittenQuery,
                null,
                null,
                null,
                3
        );
    }

    // Metadata-filtered semantic search
    public List<Document> search(String question,
                                 String category,
                                 String department,
                                 String documentType,
                                 int topK) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }

        if (topK <= 0) {
            throw new IllegalArgumentException("topK must be greater than 0");
        }

        String filter =
                buildFilter(category, department, documentType);

        logger.info("Retrieval configuration | topK={} | threshold={} | filter={}",
                topK, 0.75, filter);

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(0.75);

        if (filter != null) {
            builder.filterExpression(filter);
        }

        List<Document> results =
                vectorStore.similaritySearch(builder.build());

        logger.info("Retrieved {} chunks", results.size());

        logRetrievedChunks(results);

        logger.info("========== RETRIEVAL END ==========");

        return results;
    }

    private void logRetrievedChunks(List<Document> results) {

        if (results.isEmpty()) {
            logger.warn("NO RELEVANT CHUNKS FOUND");
            return;
        }

        for (int i = 0; i < results.size(); i++) {

            Document document = results.get(i);

            logger.info(
                    "Chunk #{} | id={} | metadata={}",
                    i + 1,
                    document.getId(),
                    document.getMetadata()
            );
        }
    }

    private String buildFilter(String category, String department,
                               String documentType) {

        StringBuilder filter = new StringBuilder();

        addFilter(filter, "category", category);
        addFilter(filter, "department", department);
        addFilter(filter, "documentType", documentType);

        return filter.isEmpty() ? null : filter.toString();
    }

    private void addFilter(StringBuilder filter, String field, String value) {

        if (value == null || value.isBlank())
            return;

        if (!filter.isEmpty())
            filter.append(" AND ");

        filter.append(field)
                .append(" == '")
                .append(value.replace("'", "''"))
                .append("'");
    }
}

