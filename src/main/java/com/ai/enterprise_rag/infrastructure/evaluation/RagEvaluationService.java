package com.ai.enterprise_rag.infrastructure.evaluation;

import com.ai.enterprise_rag.workflow.RagResult;
import com.ai.enterprise_rag.workflow.RagWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RagEvaluationService {

    private static final Logger logger =
            LoggerFactory.getLogger(RagEvaluationService.class);

    private final RagWorkflow ragWorkflow;
    private final AnswerQualityEvaluator answerQualityEvaluator;

    public RagEvaluationService(
            RagWorkflow ragWorkflow,
            AnswerQualityEvaluator answerQualityEvaluator) {

        this.ragWorkflow = ragWorkflow;
        this.answerQualityEvaluator = answerQualityEvaluator;
    }

    public List<EvaluationResult> evaluate(GoldenDataset dataset) {

        if (dataset == null || dataset.cases() == null) {
            throw new IllegalArgumentException(
                    "Golden dataset and its cases must not be null");
        }

        List<EvaluationResult> results = new ArrayList<>();

        int totalCases = 0;

        // Retrieval metrics
        int retrievalCases = 0;
        int retrievalHits = 0;

        // Answerability metrics
        int answerabilityCases = 0;
        int answerabilityCorrectCount = 0;

        // Answer-quality metrics
        int answerQualityCases = 0;

        double totalRecall = 0.0;
        double totalPrecision = 0.0;
        double totalReciprocalRank = 0.0;

        double totalFaithfulness = 0.0;
        double totalRelevance = 0.0;
        double totalCorrectness = 0.0;

        logger.info(
                "Starting RAG evaluation. Dataset={}, Version={}",
                dataset.datasetName(),
                dataset.version());

        for (GoldenTestCase testCase : dataset.cases()) {

            totalCases++;

            logger.info(
                    "Evaluating test case {}: {}",
                    testCase.id(),
                    testCase.question());

            // -------------------------------------------------
            // 1. Execute RAG workflow
            // -------------------------------------------------

            RagResult ragResult =
                    ragWorkflow.execute(testCase.question());

            if (ragResult == null) {
                throw new IllegalStateException(
                        "RagWorkflow returned null for test case "
                                + testCase.id());
            }

            // Log raw values before normalization.
            logger.info(
                    "RAW SECTION DIAGNOSTICS | case={} | expectedRaw={} "
                            + "| retrievedRaw={}",
                    testCase.id(),
                    testCase.relevantSections(),
                    ragResult.retrievedSections());

// Normalize both sides.
            List<String> expectedSections =
                    normalizeSections(testCase.relevantSections());

            List<String> retrievedSections =
                    normalizeSections(ragResult.retrievedSections());

            // Log normalized values used by the metrics.
            logger.info(
                    "NORMALIZED SECTION DIAGNOSTICS | case={} | expected={} "
                            + "| retrieved={}",
                    testCase.id(),
                    expectedSections,
                    retrievedSections);

            Set<String> expectedSectionSet =
                    new LinkedHashSet<>(expectedSections);

            // -------------------------------------------------
            // 2. Retrieval evaluation
            // -------------------------------------------------

            boolean retrievalEvaluated =
                    !expectedSectionSet.isEmpty();

            boolean hit = false;
            double recall = 0.0;
            double precision = 0.0;
            double reciprocalRank = 0.0;
            int firstRelevantRank = 0;

            if (retrievalEvaluated) {

                retrievalCases++;

                hit = RetrievalMetrics.hitRate(
                        expectedSectionSet,
                        retrievedSections);

                recall = RetrievalMetrics.recall(
                        expectedSectionSet,
                        retrievedSections);

                precision = RetrievalMetrics.precision(
                        expectedSectionSet,
                        retrievedSections);

                reciprocalRank =
                        RetrievalMetrics.reciprocalRank(
                                expectedSectionSet,
                                retrievedSections);

                firstRelevantRank =
                        RetrievalMetrics.firstRelevantRank(
                                expectedSectionSet,
                                retrievedSections);

                if (hit) {
                    retrievalHits++;
                }

                totalRecall += recall;
                totalPrecision += precision;
                totalReciprocalRank += reciprocalRank;

                logger.info(
                        "Retrieval diagnostics | case={} | expected={} "
                                + "| retrieved={} | hit={} | recall={} "
                                + "| precision={} | rank={} "
                                + "| reciprocalRank={}",
                        testCase.id(),
                        expectedSections,
                        retrievedSections,
                        hit,
                        recall,
                        precision,
                        firstRelevantRank,
                        reciprocalRank);

                if (!hit) {
                    logger.warn(
                            "Retrieval miss | case={} | question={} "
                                    + "| expected={} | retrieved={} "
                                    + "| query={}",
                            testCase.id(),
                            testCase.question(),
                            expectedSections,
                            retrievedSections,
                            ragResult.retrievalQuery());
                }

            } else {
                logger.info(
                        "Retrieval metrics skipped for case {} "
                                + "because no expected sections were configured",
                        testCase.id());
            }

            // -------------------------------------------------
            // 3. Answerability evaluation
            // -------------------------------------------------

            GroundTruth groundTruth = testCase.groundTruth();

            boolean answerabilityEvaluated =
                    groundTruth != null;

            boolean expectedAnswerable =
                    answerabilityEvaluated
                            && groundTruth.answerable();

            String generatedAnswer = ragResult.ragAnswer();

            boolean abstained = isAbstention(generatedAnswer);

            // This boolean represents the current test case.
            boolean answerabilityCorrect = false;

            if (answerabilityEvaluated) {

                answerabilityCases++;

                answerabilityCorrect =
                        expectedAnswerable
                                ? !abstained
                                : abstained;

                // This integer counts correct test cases.
                if (answerabilityCorrect) {
                    answerabilityCorrectCount++;
                }

                if (!answerabilityCorrect) {
                    logger.warn(
                            "Answerability mismatch | case={} "
                                    + "| expectedAnswerable={} "
                                    + "| abstained={} | retrieved={} "
                                    + "| answer={}",
                            testCase.id(),
                            expectedAnswerable,
                            abstained,
                            retrievedSections,
                            generatedAnswer);
                }
            }

            // -------------------------------------------------
            // 4. Answer-quality evaluation
            // -------------------------------------------------

            AnswerQualityResult qualityResult =
                    AnswerQualityResult.notEvaluated(
                            "Answer quality was not evaluated");

            boolean shouldEvaluateQuality =
                    answerabilityEvaluated
                            && expectedAnswerable
                            && !abstained;

            if (shouldEvaluateQuality) {

                qualityResult =
                        answerQualityEvaluator.evaluate(
                                testCase.question(),
                                ragResult.context(),
                                testCase.expectedAnswer(),
                                generatedAnswer);

                if (qualityResult.evaluated()) {

                    answerQualityCases++;

                    totalFaithfulness +=
                            qualityResult.faithfulnessScore();

                    totalRelevance +=
                            qualityResult.relevanceScore();

                    totalCorrectness +=
                            qualityResult.correctnessScore();

                    logger.info(
                            "Answer quality | case={} "
                                    + "| faithfulness={} "
                                    + "| relevance={} "
                                    + "| correctness={} "
                                    + "| rationale={}",
                            testCase.id(),
                            qualityResult.faithfulnessScore(),
                            qualityResult.relevanceScore(),
                            qualityResult.correctnessScore(),
                            qualityResult.rationale());
                } else {
                    logger.warn(
                            "Answer quality evaluation was not completed "
                                    + "for case {}. Reason={}",
                            testCase.id(),
                            qualityResult.rationale());
                }
            }

            // -------------------------------------------------
            // 5. Build per-test-case evaluation result
            // -------------------------------------------------

            EvaluationResult result = new EvaluationResult(
                    testCase.id(),
                    testCase.question(),
                    expectedSections,
                    retrievedSections,
                    hit,
                    recall,
                    precision,
                    reciprocalRank,
                    firstRelevantRank,
                    answerabilityEvaluated,
                    expectedAnswerable,
                    abstained,
                    answerabilityCorrect,
                    qualityResult.evaluated(),
                    qualityResult.faithfulnessScore(),
                    qualityResult.relevanceScore(),
                    qualityResult.correctnessScore(),
                    qualityResult.rationale()
            );

            results.add(result);
        }

        // -----------------------------------------------------
        // 6. Aggregate evaluation summary
        // -----------------------------------------------------

        logger.info("");
        logger.info("========================================");
        logger.info("          RAG EVALUATION SUMMARY");
        logger.info("========================================");

        logger.info("Total test cases          : {}", totalCases);
        logger.info("Retrieval evaluation cases: {}", retrievalCases);
        logger.info("Retrieval hits             : {}", retrievalHits);

        logger.info(
                "Retrieval misses           : {}",
                retrievalCases - retrievalHits);

        if (retrievalCases > 0) {

            logger.info(
                    "Hit Rate@K                 : {}",
                    format(retrievalHits, retrievalCases));

            logger.info(
                    "Recall@K                   : {}",
                    format(totalRecall, retrievalCases));

            logger.info(
                    "Precision@K                : {}",
                    format(totalPrecision, retrievalCases));

            logger.info(
                    "MRR                        : {}",
                    format(totalReciprocalRank, retrievalCases));

        } else {
            logger.warn(
                    "Retrieval metrics unavailable: no cases "
                            + "have expected sections");
        }

        logger.info(
                "Answerability cases       : {}",
                answerabilityCases);

        logger.info(
                "Answerability correct     : {}",
                answerabilityCorrectCount);

        logger.info(
                "Answerability incorrect   : {}",
                answerabilityCases - answerabilityCorrectCount);

        if (answerabilityCases > 0) {
            logger.info(
                    "Answerability accuracy    : {}",
                    format(
                            answerabilityCorrectCount,
                            answerabilityCases));
        }

        logger.info(
                "Answer quality cases      : {}",
                answerQualityCases);

        if (answerQualityCases > 0) {

            logger.info(
                    "Avg faithfulness           : {}",
                    format(totalFaithfulness, answerQualityCases));

            logger.info(
                    "Avg answer relevance       : {}",
                    format(totalRelevance, answerQualityCases));

            logger.info(
                    "Avg answer correctness     : {}",
                    format(totalCorrectness, answerQualityCases));

        } else {
            logger.warn(
                    "Answer quality averages unavailable: "
                            + "no judge evaluations succeeded");
        }

        logger.info("========================================");

        return results;
    }

    // ---------------------------------------------------------
    // Helper: normalize section identifiers
    // ---------------------------------------------------------

    private List<String> normalizeSections(List<String> sections) {

        if (sections == null || sections.isEmpty()) {
            return List.of();
        }

        return sections.stream()
                .map(SectionNormalizer::normalize)
                .filter(section -> !section.isEmpty())
                .distinct()
                .toList();
    }

    // ---------------------------------------------------------
    // Helper: identify RAG abstention responses
    // ---------------------------------------------------------

    private boolean isAbstention(String answer) {

        if (answer == null || answer.isBlank()) {
            return true;
        }

        String normalized = answer.toLowerCase(Locale.ROOT);

        return normalized.contains(
                "could not find the answer in the provided company policies")
                || normalized.contains(
                "i don't have enough information")
                || normalized.contains(
                "i do not have enough information")
                || normalized.contains(
                "i cannot determine from the provided context")
                || normalized.contains(
                "no relevant information was found");
    }

    // ---------------------------------------------------------
    // Helpers: metric formatting
    // ---------------------------------------------------------

    private String format(double numerator, double denominator) {

        if (denominator <= 0) {
            return "N/A";
        }

        return String.format(
                Locale.ROOT,
                "%.3f",
                numerator / denominator);
    }

    private String format(int numerator, int denominator) {
        return format((double) numerator, (double) denominator);
    }
}