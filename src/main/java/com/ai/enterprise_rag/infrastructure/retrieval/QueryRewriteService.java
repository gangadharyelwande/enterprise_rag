package com.ai.enterprise_rag.infrastructure.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QueryRewriteService {

    private static final Logger logger =
            LoggerFactory.getLogger(QueryRewriteService.class);

    private final ChatClient chatClient;

    public QueryRewriteService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String rewrite(String question) {

        String prompt = """
                Rewrite the following user question into a clear,
                retrieval-friendly search query.

                Rules:
                - Preserve the user's original intent.
                - Do not add facts or assumptions.
                - Keep it concise.
                - Return only the rewritten query.

                User question:
                %s
                """.formatted(question);

        try {
            String rewrittenQuery = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (rewrittenQuery == null || rewrittenQuery.isBlank()) {
                logger.warn("Query rewriting returned empty result. Using original query.");
                return question;
            }

            logger.info("Query rewrite successful | original='{}' | rewritten='{}'",
                    question, rewrittenQuery);

            return rewrittenQuery.trim();

        } catch (Exception e) {
            logger.warn("Query rewriting failed. Using original query. Error={}",
                    e.getMessage());

            return question;
        }
    }
}