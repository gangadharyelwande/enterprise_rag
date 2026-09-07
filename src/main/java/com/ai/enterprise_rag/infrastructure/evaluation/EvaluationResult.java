package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.List;

public record EvaluationResult(
        String testCaseId,
        String question,
        List<String> expectedSections,
        List<String> retrievedSections,
        boolean hit,
        double recall,
        double precision,
        double reciprocalRank,
        int firstRelevantRank
) {
}