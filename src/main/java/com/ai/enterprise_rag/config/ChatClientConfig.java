package com.ai.enterprise_rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(
            ChatClient.Builder chatClientBuilder,
            Advisor loggingAdvisor) {

        return chatClientBuilder
                .defaultAdvisors(loggingAdvisor)
                .build();
    }
}
