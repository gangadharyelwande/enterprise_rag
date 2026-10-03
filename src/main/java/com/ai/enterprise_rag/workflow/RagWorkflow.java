package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.infrastructure.retrieval.QueryRewriteService;
import com.ai.enterprise_rag.infrastructure.retrieval.RerankingService;
import org.springframework.stereotype.Component;

@Component
public class RagWorkflow implements Workflow {

    private final QueryRewriteService queryRewriteService;

    public RagWorkflow(
            QueryRewriteService queryRewriteService,
            RerankingService rerankingService) {

        this.queryRewriteService = queryRewriteService;
    }

    @Override
    public WorkflowState execute(WorkflowState state) {

        String original =
                state.getOriginalRequest();

        // Step 1: Rewrite the user's question
        String retrievalQuery =
                queryRewriteService.rewrite(original);

        // Temporary checkpoint
        System.out.println(
                "Original Query: " + original);

        System.out.println(
                "Retrieval Query: " + retrievalQuery);

        /*
         * Retrieval and answer generation will be connected
         * here using your existing RAG services.
         */

        state.setFinalAnswer(
                "RAG workflow reached successfully. "
                        + "Retrieval query: "
                        + retrievalQuery);

        return state;
    }
}