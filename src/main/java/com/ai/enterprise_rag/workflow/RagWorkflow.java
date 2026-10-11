
package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.evaluation.agent.AgentExecutionTrace;
import com.ai.enterprise_rag.infrastructure.retrieval.QueryRewriteService;
import com.ai.enterprise_rag.infrastructure.retrieval.SimilaritySearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RagWorkflow {

    private static final Logger logger =
            LoggerFactory.getLogger(RagWorkflow.class);

    private final QueryRewriteService queryRewriteService;
    private final SimilaritySearchService similaritySearchService;
    private final ChatClient chatClient;

    public RagWorkflow(
            QueryRewriteService queryRewriteService,
            SimilaritySearchService similaritySearchService,
            ChatClient chatClient) {

        this.queryRewriteService = queryRewriteService;
        this.similaritySearchService = similaritySearchService;
        this.chatClient = chatClient;
    }

    public RagResult execute(String request) {
        return execute(request, null);
    }

    public RagResult execute(String request, AgentExecutionTrace trace) {

        long startTime = System.nanoTime();

        logger.info("===== RAG WORKFLOW START =====");
        logger.info(
                "RAG workflow thread: {}",
                Thread.currentThread().getName()
        );

        try {

            // -------------------------------------------------
            // 1. Query Rewrite
            // -------------------------------------------------

            String retrievalQuery =
                    queryRewriteService.rewrite(request);

            logger.info("Original Query: {}", request);
            logger.info("Retrieval Query: {}", retrievalQuery);

            // -------------------------------------------------
            // 2. Retrieval
            // -------------------------------------------------
            // Includes:
            // - Vector search
            // - Keyword search
            // - Hybrid merge and deduplication
            // - Reranking
            // - Context compression
            // -------------------------------------------------

            List<Document> documents =
                    similaritySearchService
                            .searchRewrittenQuery(retrievalQuery);

            logger.info(
                    "Retrieved {} documents",
                    documents.size()
            );

            // -------------------------------------------------
            // 3. Extract Retrieved Sections
            // -------------------------------------------------
            // Used by RagEvaluationService to evaluate
            // retrieval quality without running retrieval again.
            // -------------------------------------------------

            List<String> retrievedSections =
                    documents.stream()
                            .map(document ->
                                    document.getMetadata().get("section"))
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .toList();

            logger.info(
                    "Retrieved sections: {}",
                    retrievedSections
            );
            if (trace != null) trace.event("RAG_RETRIEVAL", "similaritySearch", "retrievalQuery=" + retrievalQuery + "; documentCount=" + documents.size() + "; sections=" + retrievedSections, true);

            // -------------------------------------------------
            // 4. Build Context
            // -------------------------------------------------

            String context = documents.stream()
                    .map(Document::getText)
                    .collect(Collectors.joining("\n\n"));

            logger.info(
                    "RAG context length: {} characters",
                    context.length()
            );

            // -------------------------------------------------
            // 5. Generate Final Answer
            // -------------------------------------------------

            String ragAnswer =
                    generateAnswer(request, context);

            logger.info("RAG answer generated successfully");
            if (trace != null) trace.event("RAG_ANSWER", "generateAnswer", "RAG answer generated; contextLength=" + context.length(), true);

            // -------------------------------------------------
            // 6. Return Complete RAG Result
            // -------------------------------------------------
            // Query + context + retrieved sections + answer
            // -------------------------------------------------

            return new RagResult(
                    retrievalQuery,
                    context,
                    retrievedSections,
                    ragAnswer
            );

        } catch (RuntimeException ex) {
            if (trace != null) trace.event("RAG_WORKFLOW", "execute", ex.getMessage(), false);
            throw ex;
        } finally {

            long durationMillis =
                    (System.nanoTime() - startTime)
                            / 1_000_000;

            logger.info(
                    "RAG workflow END - duration={} ms - thread={}",
                    durationMillis,
                    Thread.currentThread().getName()
            );

            logger.info("===== RAG WORKFLOW END =====");
        }
    }

    private String generateAnswer(
            String question,
            String context) {

        String prompt = """
                You are an enterprise policy assistant.

                Answer the user's question using ONLY
                the information provided in the context.

                Do not invent information.

                If the context does not contain enough
                information to answer the question,
                say that you could not find the answer
                in the provided company policies.

                User Question:
                %s

                Context:
                %s
                """.formatted(
                question,
                context
        );

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}