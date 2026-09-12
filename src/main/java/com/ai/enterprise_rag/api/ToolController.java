package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.infrastructure.tools.datetime.CurrentDateTimeTool;
import com.ai.enterprise_rag.infrastructure.tools.employee.EmployeeDatabaseTool;
import com.ai.enterprise_rag.infrastructure.tools.employee.EmployeeWorkingHoursTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolController {

    private static final Logger logger =
            LoggerFactory.getLogger(ToolController.class);

    private final ChatClient chatClient;

    // Existing normal Tool Calling tools
    private final EmployeeWorkingHoursTool employeeWorkingHoursTool;
    private final EmployeeDatabaseTool employeeDatabaseTool;
    private final CurrentDateTimeTool currentDateTimeTool;

    // MCP tools discovered from MCP Server
    private final ToolCallbackProvider mcpTools;

    public ToolController(
            ChatClient chatClient,
            EmployeeWorkingHoursTool employeeWorkingHoursTool,
            EmployeeDatabaseTool employeeDatabaseTool,
            CurrentDateTimeTool currentDateTimeTool,
            ToolCallbackProvider mcpTools) {

        this.chatClient = chatClient;
        this.employeeWorkingHoursTool = employeeWorkingHoursTool;
        this.employeeDatabaseTool = employeeDatabaseTool;
        this.currentDateTimeTool = currentDateTimeTool;
        this.mcpTools = mcpTools;
    }

    /**
     * Existing normal Spring AI Tool Calling.
     */
    @GetMapping("/api/chat")
    public String chat(
            @RequestParam String message) {

        logger.info("===== NORMAL TOOL CALLING REQUEST =====");

        return chatClient
                .prompt()
                .user(message)
                .tools(
                        employeeWorkingHoursTool,
                        employeeDatabaseTool,
                        currentDateTimeTool
                )
                .call()
                .content();
    }

    /**
     * MCP-based Tool Calling.
     */
    @GetMapping("/api/mcp-chat")
    public String mcpChat(
            @RequestParam String message) {

        logger.info("===== MCP TOOL CALLING REQUEST =====");

        return chatClient
                .prompt()
                .user(message)
                .tools(mcpTools)
                .call()
                .content();
    }
}