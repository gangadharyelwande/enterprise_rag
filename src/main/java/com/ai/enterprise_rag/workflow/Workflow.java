package com.ai.enterprise_rag.workflow;

public interface Workflow {
    WorkflowState execute(WorkflowState state);
}
