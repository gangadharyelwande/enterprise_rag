package com.ai.enterprise_rag.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAgentService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeAgentService.class);

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public EmployeeAgentService(
            ChatClient chatClient,
            ToolCallbackProvider mcpTools) {

        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
    }

    public String execute(String userMessage) {

        logger.info("===== EMPLOYEE AGENT REQUEST =====");
        logger.info("User message: {}", userMessage);

        logAvailableTools();

        String response = chatClient
                .prompt()
                .system(EmployeeAgentPrompt.SYSTEM_PROMPT)
                .user(userMessage)
                .tools(mcpTools)
                .call()
                .content();

        logger.info("===== EMPLOYEE AGENT COMPLETED =====");

        return response;
    }

    private void logAvailableTools() {

        ToolCallback[] callbacks = mcpTools.getToolCallbacks();

        for (ToolCallback tool : callbacks) {
            logger.info("AGENT TOOL AVAILABLE: {}",tool.getToolDefinition().name());
        }
    }
}