package com.ai.enterprise_rag.advisor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

@Component
public class LoggingAdvisor implements CallAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @NotNull

    @Override
    public ChatClientResponse adviseCall(
            ChatClientRequest request,
            CallAdvisorChain chain) {

        long startTime = System.currentTimeMillis();

        logger.info("========== AI REQUEST START ==========");

        ChatClientResponse response =
                chain.nextCall(request);

        long duration =
                System.currentTimeMillis() - startTime;

        logger.info("AI request completed in {} ms", duration);

        if (response.chatResponse() != null) {

            var usage = response.chatResponse().getMetadata().getUsage();

            logger.info(
                    "Token usage: prompt={}, completion={}, total={}",
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens()
            );
        }

        logger.info("========== AI REQUEST END ==========");

        return response;
    }

    @NotNull
    @Override
    public String getName() {
        return "LoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}