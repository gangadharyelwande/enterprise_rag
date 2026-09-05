package com.ai.enterprise_rag.infrastructure.ingestion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkerTest {

    private final DocumentChunker documentChunker =
            new DocumentChunker();

    @Test
    @DisplayName("Should split documents into chunks")
    void shouldSplitDocumentsIntoChunks() {

        System.out.println("▶ START: shouldSplitDocumentsIntoChunks");

        Document document = new Document(
                """
                Java is widely used for enterprise applications.
                Spring Boot simplifies application development.
                Spring AI provides integration with AI models.
                Vector databases enable semantic search.
                Embeddings represent text as numerical vectors.
                Retrieval finds relevant information.
                RAG combines retrieval with generation.
                """,
                Map.of(
                        "source", "training-data",
                        "documentId", "doc-1",
                        "section", "AI Fundamentals"
                )
        );

        List<Document> chunks =
                documentChunker.chunk(List.of(document));

        assertFalse(chunks.isEmpty());

        System.out.println("✓ PASS: Chunks created = " + chunks.size());

        chunks.forEach(chunk ->
                System.out.println(
                        "Chunk → " + chunk.getText()
                )
        );

        System.out.println("◀ END: shouldSplitDocumentsIntoChunks");
    }

    @Test
    @DisplayName("Should preserve metadata when creating chunks")
    void shouldPreserveMetadata() {

        System.out.println("▶ START: shouldPreserveMetadata");

        Document document = new Document(
                """
                Java is widely used for enterprise applications.
                Spring Boot simplifies application development.
                Spring AI provides integration with AI models.
                """,
                Map.of(
                        "source", "training-data",
                        "documentId", "doc-1",
                        "section", "Java and Spring"
                )
        );

        List<Document> chunks =
                documentChunker.chunk(List.of(document));

        assertFalse(chunks.isEmpty());

        Document firstChunk = chunks.get(0);

        assertEquals(
                "training-data",
                firstChunk.getMetadata().get("source")
        );

        assertEquals(
                "doc-1",
                firstChunk.getMetadata().get("documentId")
        );

        assertEquals(
                "Java and Spring",
                firstChunk.getMetadata().get("section")
        );

        System.out.println("✓ PASS: Metadata preserved");
        System.out.println("Metadata: " + firstChunk.getMetadata());
        System.out.println("◀ END: shouldPreserveMetadata");
    }
}