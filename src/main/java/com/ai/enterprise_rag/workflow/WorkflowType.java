package com.ai.enterprise_rag.workflow;

public enum WorkflowType {
    EMPLOYEE,
    RAG,
    EMPLOYEE_AND_RAG,
    HUMAN_APPROVAL,
    UNKNOWN
}

/*
EMPLOYEE          → MCP employee workflow
RAG               → RAG workflow
EMPLOYEE_AND_RAG  → Employee + RAG in parallel
HUMAN_APPROVAL    → Sensitive action requiring human approval
UNKNOWN           → No known workflow
*/
