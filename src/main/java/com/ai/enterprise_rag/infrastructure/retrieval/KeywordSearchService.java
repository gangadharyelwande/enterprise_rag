package com.ai.enterprise_rag.infrastructure.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class KeywordSearchService {

    private static final Logger logger =
            LoggerFactory.getLogger(KeywordSearchService.class);

    public List<Document> search(String question, List<Document> documents, int topK) {

        logger.info("========== KEYWORD SEARCH START ==========");
        logger.info("Question: '{}'", question);

        // 1. Break the question into individual keywords.
        String[] keywords = question.toLowerCase().split("\\W+");

        logger.info("Extracted keywords: {}", Arrays.toString(keywords));
        logger.info("Documents available for keyword search: {}", documents.size());
        logger.info("Requested Top-K: {}", topK);

        // 2. Score each document based on keyword matches.
        List<Document> results = documents.stream()
                .peek(document -> {
                    String text = document.getText() == null
                            ? ""
                            : document.getText().toLowerCase();

                    List<String> matchedKeywords = Arrays.stream(keywords)
                            .filter(keyword -> !keyword.isBlank() && text.contains(keyword))
                            .toList();

                    long score = matchedKeywords.size();

                    document.getMetadata().put("keywordScore", score);

                    String snippet = document.getText() == null
                            ? ""
                            : document.getText().replaceAll("\\s+", " ").trim();

                    if (snippet.length() > 200) {
                        snippet = snippet.substring(0, 200) + "...";
                    }

                    logger.info(
                            "Keyword match | documentId={} | score={} | matchedKeywords={} | text='{}'",
                            document.getId(),
                            score,
                            matchedKeywords,
                            snippet
                    );
                })

                // 3. Remove documents with no keyword matches.
                .filter(document ->
                        ((Number) document.getMetadata()
                                .get("keywordScore"))
                                .longValue() > 0)

                // 4. Highest keyword score first.
                .sorted(Comparator.comparingLong(document ->
                        -((Number) document.getMetadata()
                                .get("keywordScore"))
                                .longValue()))

                // 5. Keep only Top-K results.
                .limit(topK)
                .toList();

        logger.info("Final keyword results: {}", results.size());

        // Log final ranking.
        for (int i = 0; i < results.size(); i++) {
            Document document = results.get(i);

            logger.info(
                    "Keyword result #{} | documentId={} | score={}",
                    i + 1,
                    document.getId(),
                    document.getMetadata().get("keywordScore")
            );
        }

        logger.info("========== KEYWORD SEARCH END ==========");

        return results;
    }
}