package com.ai.enterprise_rag.infrastructure.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ContextCompressionService {

    private static final Logger logger =
            LoggerFactory.getLogger(ContextCompressionService.class);

    private final ChatClient chatClient;

    public ContextCompressionService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<Document> compress(String question, List<Document> documents) {

        logger.info("========== CONTEXT COMPRESSION START ==========");
        logger.info("Question: '{}'", question);
        logger.info("Documents before compression: {}", documents.size());

        List<Document> compressedDocuments = new ArrayList<>();

        for (Document document : documents) {

            String originalText = document.getText();

            assert originalText != null;
            logger.info(
                    "BEFORE | documentId={} | chars={} | text='{}'",
                    document.getId(),
                    originalText.length(),
                    shorten(originalText)
            );

            String prompt = """
                    Extract only the information from the document
                    that is directly relevant to answering the question.

                    Do not add new information.
                    Do not change facts.
                    If nothing is relevant, return an empty response.

                    Question:
                    %s

                    Document:
                    %s
                    """.formatted(question, originalText);

            try {
                String compressedText = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                if (compressedText == null || compressedText.isBlank()) {
                    logger.info(
                            "COMPRESSION | documentId={} | no relevant content",
                            document.getId()
                    );
                    continue;
                }

                Document compressedDocument =
                        new Document(compressedText, document.getMetadata());

                logger.info(
                        "AFTER  | documentId={} | chars={} | text='{}'",
                        document.getId(),
                        compressedText.length(),
                        shorten(compressedText)
                );

                compressedDocuments.add(compressedDocument);

            } catch (Exception e) {
                logger.warn(
                        "Compression failed | documentId={} | error={}",
                        document.getId(),
                        e.getMessage()
                );

                // Keep the original chunk if compression fails.
                compressedDocuments.add(document);
            }
        }

        logger.info(
                "Documents after compression: {}",
                compressedDocuments.size()
        );

        logger.info("========== CONTEXT COMPRESSION END ==========");

        return compressedDocuments;
    }

    private String shorten(String text) {
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 250
                ? cleaned.substring(0, 250) + "..."
                : cleaned;
    }
}