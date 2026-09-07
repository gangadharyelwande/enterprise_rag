package com.ai.enterprise_rag.evaluation;

import com.ai.enterprise_rag.infrastructure.evaluation.EvaluationResult;
import com.ai.enterprise_rag.infrastructure.evaluation.GoldenDataset;
import com.ai.enterprise_rag.infrastructure.evaluation.GoldenDatasetLoader;
import com.ai.enterprise_rag.infrastructure.evaluation.RagEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class RagEvaluationTest {

    @Autowired
    private GoldenDatasetLoader goldenDatasetLoader;

    @Autowired
    private RagEvaluationService ragEvaluationService;

    @Test
    void evaluateGoldenDataset() {

        GoldenDataset dataset =
                goldenDatasetLoader.load();

        assertFalse(
                dataset.cases().isEmpty(),
                "Golden dataset must contain test cases"
        );

        List<EvaluationResult> results =
                ragEvaluationService.evaluate(
                        dataset
                );

        assertFalse(
                results.isEmpty(),
                "Evaluation should produce results"
        );
    }
}