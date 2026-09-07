package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.List;

public record GroundTruth(
        String sourceEvidence,
        List<String> expectedEvidence,
        boolean answerable
) {}