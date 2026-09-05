package com.ai.enterprise_rag.infrastructure.ingestion;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentMetadataEnricher {

    public List<Document> enrich(List<Document> documents) {

        documents.forEach(document -> {

            // Document-level metadata
            document.getMetadata().put("documentType", "HR_POLICY");
            document.getMetadata().put("category", "HR");
            document.getMetadata().put("department", "HUMAN_RESOURCES");
            document.getMetadata().put("country", "USA");
            document.getMetadata().put("version", "1.0");

            // Demo values for learning purposes.
            // In production, these should come from the document/database.
            document.getMetadata().put("effectiveDate", "2026-01-01");

            // Enterprise/security-related metadata
            document.getMetadata().put("accessLevel", "EMPLOYEE");
            document.getMetadata().put("tenantId", "EAZYBYTES");
        });

        return documents;
    }
}