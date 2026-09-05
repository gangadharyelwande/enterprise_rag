package com.ai.enterprise_rag.infrastructure.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SentenceDocumentLoader {

    private static final Logger log =
            LoggerFactory.getLogger(SentenceDocumentLoader.class);

    private final TextCleaner textCleaner;

    public SentenceDocumentLoader(TextCleaner textCleaner) {
        this.textCleaner = textCleaner;
    }

    // Flow: Raw source documents → clean text → create Spring AI Documents with metadata.
    // These Documents are the input for the next RAG steps: chunking → embedding → vector store.
    public List<Document> load() {

        log.info("▶ START: Loading source documents");

        List<String> rawDocuments = List.of(

                """
                Java is widely used for enterprise applications.
                Spring Boot simplifies application development.
                Spring AI provides integration with AI models.
                Java applications can be deployed using containers.
                Docker packages applications into lightweight containers.
                Kubernetes automates container orchestration at scale.
                """,

                """
                Vector databases enable semantic search.
                Embeddings represent text as numerical vectors.
                Similarity search finds documents that are semantically related.
                Metadata can be stored along with each document.
                Metadata helps filter and identify retrieved information.
                """,

                """
                Retrieval Augmented Generation combines retrieval with generation.
                A RAG system first retrieves relevant information.
                The retrieved information is then provided to the language model.
                The language model generates an answer using the retrieved context.
                Good retrieval is important for producing grounded answers.
                """
        );

        List<Document> documents = new java.util.ArrayList<>();

        for (int i = 0; i < rawDocuments.size(); i++) {

            String cleanedText = textCleaner.clean(rawDocuments.get(i));

            if (cleanedText.isBlank()) {
                continue;
            }

            Document document = new Document(
                    cleanedText,
                    Map.of(
                            "source", "training-data",
                            "documentId", "doc-" + (i + 1),
                            "section", getSectionName(i)
                    )
            );

            documents.add(document);

            log.info(
                    "✓ Created document: documentId={}, section={}",
                    "doc-" + (i + 1),
                    getSectionName(i)
            );
        }

        log.info("Total documents created: {}", documents.size());
        log.info("◀ END: Loading source documents");

        return documents;
    }

    private String getSectionName(int index) {

        return switch (index) {
            case 0 -> "Java and Spring";
            case 1 -> "Embeddings and Vector Search";
            case 2 -> "RAG Fundamentals";
            default -> "Unknown";
        };
    }
}