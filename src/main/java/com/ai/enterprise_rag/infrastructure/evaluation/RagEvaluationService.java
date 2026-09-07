package com.ai.enterprise_rag.infrastructure.evaluation;

import com.ai.enterprise_rag.infrastructure.retrieval.SimilaritySearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagEvaluationService {

    private static final Logger logger =
            LoggerFactory.getLogger(RagEvaluationService.class);

    private final SimilaritySearchService searchService;

    public RagEvaluationService(SimilaritySearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * Runs the Golden Dataset against the RAG retrieval pipeline.
     */
    public List<EvaluationResult> evaluate(GoldenDataset dataset) {

        logger.info("========== RAG EVALUATION START ==========");
        logger.info("Dataset: {}", dataset.datasetName());
        logger.info("Version: {}", dataset.version());
        logger.info("Test cases: {}", dataset.cases().size());

        List<EvaluationResult> results = new ArrayList<>();

        for (GoldenTestCase testCase : dataset.cases()) {
            EvaluationResult result = evaluateTestCase(testCase);
            results.add(result);
            logResult(result);
        }

        printSummary(results);

        logger.info("========== RAG EVALUATION END ==========");

        return results;
    }

    /**
     * Evaluates one Golden Dataset test case.
     */
    private EvaluationResult evaluateTestCase(GoldenTestCase testCase) {

        logger.info("");
        logger.info("Evaluating {} | {}", testCase.id(), testCase.question());

        // Run the actual RAG retrieval pipeline.
        List<Document> documents =
                searchService.search(testCase.question());

        // Extract section metadata from retrieved chunks.
        List<String> retrievedSections =
                extractSections(documents);

        // Get expected sections from Ground Truth.
        Set<String> expectedSections =
                normalizeSections(testCase.relevantSections());

        boolean hit;
        double recall;
        double precision;
        double reciprocalRank;
        int firstRelevantRank;

        /*
         * No-answer test case.
         * Normal retrieval metrics are not calculated.
         */
        if (expectedSections.isEmpty()) {

            hit = documents.isEmpty();
            recall = 0.0;
            precision = 0.0;
            reciprocalRank = 0.0;
            firstRelevantRank = 0;

        } else {

            hit = RetrievalMetrics.hitRate(
                    expectedSections,
                    retrievedSections
            );

            recall = RetrievalMetrics.recall(
                    expectedSections,
                    retrievedSections
            );

            precision = RetrievalMetrics.precision(
                    expectedSections,
                    retrievedSections
            );

            reciprocalRank = RetrievalMetrics.reciprocalRank(
                    expectedSections,
                    retrievedSections
            );

            firstRelevantRank = RetrievalMetrics.firstRelevantRank(
                    expectedSections,
                    retrievedSections
            );
        }

        return new EvaluationResult(
                testCase.id(),
                testCase.question(),
                new ArrayList<>(expectedSections),
                retrievedSections,
                hit,
                recall,
                precision,
                reciprocalRank,
                firstRelevantRank
        );
    }

    /**
     * Extract section numbers from retrieved documents.
     * Example: 4 → §4
     */
    private List<String> extractSections(List<Document> documents) {

        return documents.stream()
                .map(document -> document.getMetadata().get("section"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(this::normalizeSection)
                .toList();
    }

    /**
     * Normalizes expected sections.
     * Example: "4" → "§4"
     */
    private Set<String> normalizeSections(List<String> sections) {

        Set<String> normalized = new LinkedHashSet<>();

        if (sections == null) {
            return normalized;
        }

        for (String section : sections) {

            if (section == null || section.isBlank()) {
                continue;
            }

            normalized.add(normalizeSection(section));
        }

        return normalized;
    }

    private String normalizeSection(String section) {

        String normalized = section.trim();

        normalized = normalized.replace("§", "").trim();

        int spaceIndex = normalized.indexOf(" ");

        if (spaceIndex > 0) {
            normalized = normalized.substring(0, spaceIndex);
        }

        return "§" + normalized;
    }

    /**
     * Logs the result for each test case.
     */
    private void logResult(EvaluationResult result) {

        logger.info(
                "RESULT | id={} | hit={} | recall={} | precision={} | MRR={} | firstRelevantRank={}",
                result.testCaseId(),
                result.hit(),
                format(result.recall()),
                format(result.precision()),
                format(result.reciprocalRank()),
                result.firstRelevantRank()
        );

        logger.info(
                "EXPECTED SECTIONS | {}",
                result.expectedSections()
        );

        logger.info(
                "RETRIEVED SECTIONS | {}",
                result.retrievedSections()
        );
    }

    /**
     * Prints aggregate retrieval metrics.
     */
    private void printSummary(List<EvaluationResult> results) {

        if (results.isEmpty()) {
            return;
        }

        // No-answer questions are excluded from retrieval metrics.
        List<EvaluationResult> answerableResults =
                results.stream()
                        .filter(result ->
                                !result.expectedSections().isEmpty())
                        .toList();

        if (answerableResults.isEmpty()) {
            logger.info(
                    "No answerable test cases available for retrieval metrics."
            );
            return;
        }

        double hitRate =
                answerableResults.stream()
                        .mapToDouble(result -> result.hit() ? 1.0 : 0.0)
                        .average()
                        .orElse(0.0);

        double recall =
                answerableResults.stream()
                        .mapToDouble(EvaluationResult::recall)
                        .average()
                        .orElse(0.0);

        double precision =
                answerableResults.stream()
                        .mapToDouble(EvaluationResult::precision)
                        .average()
                        .orElse(0.0);

        double mrr =
                answerableResults.stream()
                        .mapToDouble(EvaluationResult::reciprocalRank)
                        .average()
                        .orElse(0.0);

        long passed =
                answerableResults.stream()
                        .filter(EvaluationResult::hit)
                        .count();

        long failed = answerableResults.size() - passed;

        logger.info("");
        logger.info("============================================");
        logger.info("RAG EVALUATION SUMMARY");
        logger.info("============================================");
        logger.info("Total test cases       : {}", results.size());
        logger.info("Answerable test cases : {}", answerableResults.size());
        logger.info("Passed                 : {}", passed);
        logger.info("Failed                 : {}", failed);
        logger.info("Hit Rate@3             : {}", format(hitRate));
        logger.info("Recall@3               : {}", format(recall));
        logger.info("Precision@3            : {}", format(precision));
        logger.info("MRR                    : {}", format(mrr));
        logger.info("============================================");
    }

    private String format(double value) {
        return String.format("%.3f", value);
    }
}