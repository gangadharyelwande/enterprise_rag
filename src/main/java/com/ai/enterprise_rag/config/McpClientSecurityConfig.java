package com.ai.enterprise_rag.config;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpClientSecurityConfig {

    @Bean
    McpSyncHttpClientRequestCustomizer mcpApiKeyCustomizer(
            @Value("${mcp.server.api-key}") String apiKey) {

        System.out.println( "MCP CLIENT API KEY CONFIGURED = " + apiKey );
        return (builder, method, endpoint, body, context) -> {
            System.out.println(
                    "ADDING MCP API KEY HEADER: "
                            + method
                            + " "
                            + endpoint
            );

            builder.header(
                    "X-MCP-API-KEY",
                    apiKey
            );
        };
    }
}

