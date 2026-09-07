package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.List;

public record GoldenDataset(
        String datasetName,
        String version,
        SourceDocument sourceDocument,
        List<GoldenTestCase> cases
) {}