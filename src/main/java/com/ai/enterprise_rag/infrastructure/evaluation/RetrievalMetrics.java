
package com.ai.enterprise_rag.infrastructure.evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class RetrievalMetrics {

    private RetrievalMetrics() {
    }

    public static boolean hitRate(
            Set<String> expected,
            List<String> retrieved) {

        if (expected == null
                || expected.isEmpty()
                || retrieved == null
                || retrieved.isEmpty()) {
            return false;
        }

        Set<String> normalizedExpected =
                normalizeExpected(expected);

        return retrieved.stream()
                .map(SectionNormalizer::normalize)
                .filter(section -> !section.isEmpty())
                .anyMatch(normalizedExpected::contains);
    }

    public static double recall(
            Set<String> expected,
            List<String> retrieved) {

        if (expected == null || expected.isEmpty()) {
            return 0.0;
        }

        Set<String> normalizedExpected =
                normalizeExpected(expected);

        if (retrieved == null || retrieved.isEmpty()) {
            return 0.0;
        }

        Set<String> normalizedRetrieved = new HashSet<>();

        for (String section : retrieved) {
            String normalized =
                    SectionNormalizer.normalize(section);

            if (!normalized.isEmpty()) {
                normalizedRetrieved.add(normalized);
            }
        }

        long relevantRetrieved = normalizedRetrieved.stream()
                .filter(normalizedExpected::contains)
                .count();

        return (double) relevantRetrieved
                / normalizedExpected.size();
    }

    public static double precision(
            Set<String> expected,
            List<String> retrieved) {

        if (expected == null
                || expected.isEmpty()
                || retrieved == null
                || retrieved.isEmpty()) {
            return 0.0;
        }

        Set<String> normalizedExpected =
                normalizeExpected(expected);

        long validRetrievedCount = 0;
        long relevantRetrievedCount = 0;

        for (String section : retrieved) {
            String normalized =
                    SectionNormalizer.normalize(section);

            if (normalized.isEmpty()) {
                continue;
            }

            validRetrievedCount++;

            if (normalizedExpected.contains(normalized)) {
                relevantRetrievedCount++;
            }
        }

        if (validRetrievedCount == 0) {
            return 0.0;
        }

        return (double) relevantRetrievedCount
                / validRetrievedCount;
    }

    public static int firstRelevantRank(
            Set<String> expected,
            List<String> retrieved) {

        if (expected == null
                || expected.isEmpty()
                || retrieved == null) {
            return 0;
        }

        Set<String> normalizedExpected =
                normalizeExpected(expected);

        for (int i = 0; i < retrieved.size(); i++) {
            String normalized =
                    SectionNormalizer.normalize(retrieved.get(i));

            if (!normalized.isEmpty()
                    && normalizedExpected.contains(normalized)) {
                return i + 1;
            }
        }

        return 0;
    }

    public static double reciprocalRank(
            Set<String> expected,
            List<String> retrieved) {

        int rank = firstRelevantRank(expected, retrieved);

        return rank == 0 ? 0.0 : 1.0 / rank;
    }

    private static Set<String> normalizeExpected(
            Set<String> expected) {

        Set<String> normalized = new HashSet<>();

        for (String section : expected) {
            String value = SectionNormalizer.normalize(section);

            if (!value.isEmpty()) {
                normalized.add(value);
            }
        }

        return normalized;
    }
}