package com.ai.enterprise_rag.infrastructure.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentChunker {

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentChunker.class);

    private final TextSplitter splitter;

    public DocumentChunker() {

        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(200)
                .withMaxNumChunks(400)
                .build();
    }

    public List<Document> chunk(List<Document> documents) {

        List<Document> chunks =
                splitter.split(documents);

        logger.info(
                "Chunking completed: {} documents → {} chunks",
                documents.size(),
                chunks.size()
        );

        return chunks;
    }
}