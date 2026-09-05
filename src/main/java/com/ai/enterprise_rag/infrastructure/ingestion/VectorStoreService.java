package com.ai.enterprise_rag.infrastructure.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private static final Logger logger =
            LoggerFactory.getLogger(VectorStoreService.class);

    private final VectorStore vectorStore;

    public VectorStoreService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void store(List<Document> chunks) {

        // Spring AI creates embeddings and stores chunks in Vector DB.
        vectorStore.add(chunks);

        logger.info("Stored {} chunks in vector database", chunks.size());

//        Document Chunks
//              ↓
//        vectorStore.add(chunks)
//              ↓
//        EmbeddingModel
//              ↓
//        Embedding API / Model
//              ↓
//        Vector (numbers)
//              ↓
//        Qdrant Vector Database
//              ↓
//        Vector + Chunk Text + Metadata
    }
}
