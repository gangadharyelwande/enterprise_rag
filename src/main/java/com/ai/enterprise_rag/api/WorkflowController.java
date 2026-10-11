package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.workflow.WorkflowOrchestrator;
import com.ai.enterprise_rag.workflow.WorkflowResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkflowController {

    private final WorkflowOrchestrator orchestrator;

    public WorkflowController(
            WorkflowOrchestrator orchestrator) {

        this.orchestrator = orchestrator;
    }

    @PostMapping("/workflow-chat")
    public WorkflowResponse chat(
            @RequestParam String message) {

        return orchestrator.execute(message);
    }

    @PostMapping("/workflow/chat")
    public WorkflowResponse workFlowChat(
            @RequestParam String message) {

        return orchestrator.execute(message);
    }
}

