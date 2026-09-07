package com.ai.enterprise_rag.infrastructure.ingestion;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PdfDocumentLoader {

    private static final Logger logger =
            LoggerFactory.getLogger(PdfDocumentLoader.class);

    private final DocumentChunker documentChunker;
    private final DocumentMetadataEnricher metadataEnricher;
    private final VectorStoreService vectorStoreService;

    @Value("classpath:Eazybytes_HR_Policies.pdf")
    private Resource policyFile;

    public PdfDocumentLoader(
            DocumentChunker documentChunker,
            DocumentMetadataEnricher metadataEnricher,
            VectorStoreService vectorStoreService) {

        this.documentChunker = documentChunker;
        this.metadataEnricher = metadataEnricher;
        this.vectorStoreService = vectorStoreService;
    }

    @PostConstruct
    public void loadPDF() {

        // PDF → Documents
        TikaDocumentReader reader =
                new TikaDocumentReader(policyFile);

        List<Document> documents =
                reader.get();

        logger.info(
                "PDF loaded: {} documents",
                documents.size()
        );

        // Add document-level metadata
        logger.info("Adding/Enriching Metadata");

        documents =
                metadataEnricher.enrich(documents);

        // Documents → Section-aware Chunks
        logger.info("Documents Chunking");

        List<Document> chunks =
                documentChunker.chunk(documents);

        // Chunks → Embeddings → Vector DB
        vectorStoreService.store(chunks);

        logger.info(
                "Ingestion completed: {} chunks stored",
                chunks.size()
        );
    }
}