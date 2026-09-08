package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.infrastructure.tools.EmployeeDatabaseTool;
import com.ai.enterprise_rag.infrastructure.tools.EmployeeWorkingHoursTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolController {
    private static final Logger logger =
            LoggerFactory.getLogger(ToolController.class);

    private final ChatClient chatClient;
    private final EmployeeWorkingHoursTool employeeWorkingHoursTool;
    private final EmployeeDatabaseTool employeeDatabaseTool;

    public ToolController(
            ChatClient chatClient,
            EmployeeWorkingHoursTool employeeWorkingHoursTool,
            EmployeeDatabaseTool employeeDatabaseTool) {

        this.chatClient = chatClient;
        this.employeeWorkingHoursTool = employeeWorkingHoursTool;
        this.employeeDatabaseTool = employeeDatabaseTool;
    }

    @GetMapping("/api/chat")
    public String chat(
            @RequestParam String message) {

        return chatClient
                .prompt()
                .user(message)
                .tools(employeeWorkingHoursTool, employeeDatabaseTool)
                .call()
                .content();
    }
}
