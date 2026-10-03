package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.agent.EmployeeAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final EmployeeAgentService employeeAgentService;

    public AgentController(
            EmployeeAgentService employeeAgentService) {

        this.employeeAgentService = employeeAgentService;
    }

    @GetMapping("/api/agent-chat")
    public String agentChat(
            @RequestParam String message) {

        return employeeAgentService.execute(message);
    }
}