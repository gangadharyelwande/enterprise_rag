package com.ai.enterprise_rag.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WorkflowOrchestrator {

    private static final Logger logger =
            LoggerFactory.getLogger(WorkflowOrchestrator.class);

    private final WorkflowRouter router;
    private final EmployeeWorkflow employeeWorkflow;
    private final RagWorkflow ragWorkflow;

    public WorkflowOrchestrator(
            WorkflowRouter router,
            EmployeeWorkflow employeeWorkflow,
            RagWorkflow ragWorkflow) {

        this.router = router;
        this.employeeWorkflow = employeeWorkflow;
        this.ragWorkflow = ragWorkflow;
    }

    public WorkflowState execute(String request) {

        logger.info("===== WORKFLOW EXECUTION START =====");
        logger.info("Incoming request: {}", request);

        WorkflowState state =
                new WorkflowState(request);

        WorkflowType type =
                router.route(request);

        logger.info("Workflow selected: {}", type);

        state.setWorkflowType(type);

        WorkflowState result = switch (type) {

            case EMPLOYEE -> {
                logger.info("Executing Employee Workflow");
                yield employeeWorkflow.execute(state);
            }

            case RAG -> {
                logger.info("Executing RAG Workflow");
                yield ragWorkflow.execute(state);
            }

            case UNKNOWN -> {
                logger.warn(
                        "No workflow found for request: {}",
                        request);

                throw new IllegalArgumentException(
                        "No workflow found for request");
            }
        };

        logger.info(
                "Workflow execution completed: {}",
                type);

        logger.info("===== WORKFLOW EXECUTION END =====");

        return result;
    }
}