package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.List;

public record GoldenTestCase(
        String id,
        String question,
        String type,
        List<String> relevantSections,
        GroundTruth groundTruth,
        String expectedAnswer,
        boolean validated,
        String validationNotes,
        String evaluationNotes
) {
}