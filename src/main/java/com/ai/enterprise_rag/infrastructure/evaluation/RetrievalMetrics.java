package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.List;
import java.util.Set;

public final class RetrievalMetrics {

    private RetrievalMetrics() {
    }

    public static boolean hitRate(
            Set<String> expected,
            List<String> retrieved) {

        return retrieved.stream()
                .anyMatch(expected::contains);
    }

    public static double recall(
            Set<String> expected,
            List<String> retrieved) {

        if (expected.isEmpty()) {
            return 0.0;
        }

        long relevantRetrieved =
                retrieved.stream()
                        .filter(expected::contains)
                        .distinct()
                        .count();

        return (double) relevantRetrieved
                / expected.size();
    }

    public static double precision(
            Set<String> expected,
            List<String> retrieved) {

        if (retrieved.isEmpty()) {
            return 0.0;
        }

        long relevantRetrieved =
                retrieved.stream()
                        .filter(expected::contains)
                        .count();

        return (double) relevantRetrieved
                / retrieved.size();
    }

    public static int firstRelevantRank(
            Set<String> expected,
            List<String> retrieved) {

        for (int i = 0; i < retrieved.size(); i++) {

            if (expected.contains(retrieved.get(i))) {
                return i + 1;
            }
        }

        return 0;
    }

    public static double reciprocalRank(
            Set<String> expected,
            List<String> retrieved) {

        int rank =
                firstRelevantRank(expected, retrieved);

        if (rank == 0) {
            return 0.0;
        }

        return 1.0 / rank;
    }
}