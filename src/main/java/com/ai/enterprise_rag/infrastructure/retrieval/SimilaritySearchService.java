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

    public SimilaritySearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Basic semantic search
    public List<Document> search(String question) {
        return search(question, null, null, null, 3);
    }

    // Metadata-filtered semantic search
    public List<Document> search(String question, String category,
                                 String department, String documentType,
                                 int topK) {

        if (topK <= 0)
            throw new IllegalArgumentException("topK must be greater than 0");

        String filter = buildFilter(category, department, documentType);

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(question)
                .topK(topK)
                .similarityThreshold(0.75);

        if (filter != null)
            builder.filterExpression(filter);

        List<Document> results =
                vectorStore.similaritySearch(builder.build());

        logger.info("Question: '{}', Top-K: {}, Filter: {}, Results: {}",
                question, topK, filter, results.size());

        return results;
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

