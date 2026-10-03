package com.ai.enterprise_rag.api;

import com.ai.enterprise_rag.workflow.WorkflowOrchestrator;
import com.ai.enterprise_rag.workflow.WorkflowState;
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
    public WorkflowState chat(
            @RequestParam String message) {

        return orchestrator.execute(message);
    }
}
