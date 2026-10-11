package com.ai.enterprise_rag.agent;

import com.ai.enterprise_rag.agent.tools.EnterpriseRagTool;
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
    private final EnterpriseRagTool enterpriseRagTool;

    public EmployeeAgentService(
            ChatClient chatClient,
            ToolCallbackProvider mcpTools,
            EnterpriseRagTool enterpriseRagTool) {

        this.chatClient = chatClient;
        this.mcpTools = mcpTools;
        this.enterpriseRagTool = enterpriseRagTool;
    }

    public String execute(String userMessage) {

        logger.info("===== EMPLOYEE AGENT REQUEST =====");
        logger.info("User message: {}", userMessage);

        for (ToolCallback tool : mcpTools.getToolCallbacks()) {

            logger.info(
                    "AGENT MCP TOOL AVAILABLE: {}",
                    tool.getToolDefinition().name()
            );
        }

        logger.info(
                "AGENT RAG TOOL AVAILABLE: searchEnterpriseKnowledge"
        );

        String response = chatClient
                .prompt()
                .system(EmployeeAgentPrompt.SYSTEM_PROMPT)
                .user(userMessage)
                .tools(
                        enterpriseRagTool,
                        mcpTools
                )
                .call()
                .content();

        logger.info("===== EMPLOYEE AGENT COMPLETED =====");

        return response;
    }
}