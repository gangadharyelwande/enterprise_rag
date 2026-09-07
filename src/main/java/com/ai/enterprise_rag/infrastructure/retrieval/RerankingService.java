package com.ai.enterprise_rag.infrastructure.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RerankingService {

    private static final Logger logger =
            LoggerFactory.getLogger(RerankingService.class);

    private final ChatClient chatClient;

    public RerankingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<Document> rerank(
            String question,
            List<Document> candidates,
            int finalTopK) {
        System.out.println("finalTopK==>"+finalTopK);

        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<ScoredDocument> scoredDocuments = new ArrayList<>();

        for (Document document : candidates) {

            String prompt = """
                    Rate how relevant the following document is
                    to the user's question.

                    Return ONLY a number from 0 to 10.

                    Question:
                    %s

                    Document:
                    %s
                    """.formatted(
                    question,
                    document.getText()
            );

            try {

                String response = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                assert response != null;
                double score =
                        Double.parseDouble(response.trim());

                document.getMetadata().put(
                        "rerankScore",
                        score
                );

                scoredDocuments.add(new ScoredDocument(document, score));

                logger.info(
                        "Rerank | documentId={} | score={}",
                        document.getId(),
                        score
                );

            } catch (Exception e) {

                logger.warn(
                        "Reranking failed | documentId={} | error={}",
                        document.getId(),
                        e.getMessage()
                );

                scoredDocuments.add(
                        new ScoredDocument(document, 0)
                );
            }
        }

        return scoredDocuments.stream()
                .sorted(
                        Comparator.comparingDouble(
                                ScoredDocument::score
                        ).reversed()
                )
                .limit(finalTopK)
                .map(ScoredDocument::document)
                .toList();
    }

    private record ScoredDocument(
            Document document,
            double score) {
    }
}