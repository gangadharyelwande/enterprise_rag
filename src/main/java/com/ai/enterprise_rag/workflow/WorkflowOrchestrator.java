package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.evaluation.agent.AgentExecutionTrace;
import com.ai.enterprise_rag.guardrail.GuardrailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class WorkflowOrchestrator {

    private static final Logger logger =
            LoggerFactory.getLogger(WorkflowOrchestrator.class);

    private final WorkflowRouter router;
    private final EmployeeWorkflow employeeWorkflow;
    private final RagWorkflow ragWorkflow;
    private final AgentWorkflowService agentWorkflowService;
    private final ExecutorService workflowExecutor;
    private final ApprovalStore approvalStore;
    private final GuardrailService guardrailService;

    public WorkflowOrchestrator(
            WorkflowRouter router,
            EmployeeWorkflow employeeWorkflow,
            RagWorkflow ragWorkflow,
            AgentWorkflowService agentWorkflowService,
            ExecutorService workflowExecutor,
            ApprovalStore approvalStore,
            GuardrailService guardrailService) {

        this.router = router;
        this.employeeWorkflow = employeeWorkflow;
        this.ragWorkflow = ragWorkflow;
        this.agentWorkflowService = agentWorkflowService;
        this.workflowExecutor = workflowExecutor;
        this.approvalStore = approvalStore;
        this.guardrailService = guardrailService;
    }

    public WorkflowResponse execute(String request) {
        return execute(request, null);
    }

    /** Executes the normal workflow while optionally recording evaluation trace events. */
    public WorkflowResponse execute(String request, AgentExecutionTrace trace) {

        logger.info("========================================");
        logger.info("WORKFLOW ORCHESTRATOR START");
        logger.info("Request: {}", request);
        logger.info("========================================");

        // -------------------------------------------------
        // Step 1: Create workflow state
        // -------------------------------------------------

        WorkflowState state =
                new WorkflowState(request);

        // -------------------------------------------------
        // Step 2: Determine workflow type
        // -------------------------------------------------

        WorkflowType type =
                router.route(request);

        state.setWorkflowType(type);
        if (trace != null) {
            trace.setWorkflowType(type.name());
            trace.event("WORKFLOW_ROUTE", type.name(), "WorkflowRouter selected " + type.name(), true);
        }

        logger.info(
                "Selected workflow type: {}",
                type
        );

        // -------------------------------------------------
        // Step 3: Extract employee ID if present
        // -------------------------------------------------

        String employeeId =
                router.extractEmployeeId(request);

        if (employeeId != null) {

            state.setEmployeeId(employeeId);

            logger.info(
                    "Employee ID: {}",
                    employeeId
            );
        }

        // -------------------------------------------------
        // Step 4: Execute appropriate workflow
        // -------------------------------------------------

        switch (type) {

            // =============================================
            // HUMAN APPROVAL
            // =============================================

            case HUMAN_APPROVAL -> {

                if (trace != null) trace.event("HUMAN_APPROVAL", "createApproval", "Approval requested; execution paused for human decision", true);
                logger.info(
                        "===== HUMAN APPROVAL REQUIRED ====="
                );

                logger.info(
                        "Human approval required for request: {}",
                        request
                );

                PendingApproval approval =
                        approvalStore.create(
                                request,
                                "APPROVE_LEAVE"
                        );

                logger.info(
                        "Approval created: approvalId={}, status={}",
                        approval.approvalId(),
                        approval.status()
                );

                logger.info(
                        "Workflow paused waiting for human approval"
                );

                logger.info(
                        "===== WORKFLOW PAUSED ====="
                );

                return new WorkflowResponse(
                        "PENDING_APPROVAL",
                        null,
                        approval.approvalId(),
                        "Human approval is required before approving leave."
                );
            }

            // =============================================
            // EMPLOYEE ONLY
            // =============================================

            case EMPLOYEE -> {

                logger.info(
                        "Executing EMPLOYEE workflow"
                );

                EmployeeResult employeeResult =
                        employeeWorkflow.execute(request, trace);

                state.setEmployeeResult(
                        employeeResult.employeeData()
                );

                logger.info(
                        "Employee result stored in WorkflowState"
                );
            }

            // =============================================
            // RAG ONLY
            // =============================================

            case RAG -> {

                logger.info(
                        "Executing RAG workflow"
                );

                RagResult ragResult =
                        ragWorkflow.execute(request, trace);

                state.setRagResult(
                        ragResult.ragAnswer()
                );

                logger.info(
                        "RAG result stored in WorkflowState"
                );
            }

            // =============================================
            // EMPLOYEE + RAG
            // =============================================

            case EMPLOYEE_AND_RAG -> {

                logger.info(
                        "Executing EMPLOYEE + RAG workflows in parallel"
                );

                CompletableFuture<EmployeeResult>
                        employeeFuture =
                        CompletableFuture.supplyAsync(
                                () -> employeeWorkflow.execute(request, trace),
                                workflowExecutor
                        );

                CompletableFuture<RagResult>
                        ragFuture =
                        CompletableFuture.supplyAsync(
                                () -> ragWorkflow.execute(request, trace),
                                workflowExecutor
                        );

                EmployeeResult employeeResult =
                        employeeFuture.join();

                RagResult ragResult =
                        ragFuture.join();

                logger.info(
                        "Both parallel workflows completed"
                );

                state.setEmployeeResult(
                        employeeResult.employeeData()
                );

                state.setRagResult(
                        ragResult.ragAnswer()
                );

                logger.info(
                        "Employee result and RAG result merged into WorkflowState"
                );
            }

            // =============================================
            // UNKNOWN
            // =============================================

            case UNKNOWN -> {

                throw new IllegalArgumentException(
                        "Unsupported workflow request: "
                                + request
                );
            }
        }

        // -------------------------------------------------
        // Step 5: Validate aggregated state
        // -------------------------------------------------

        logger.info(
                "===== AGGREGATED WORKFLOW STATE ====="
        );

        logger.info(
                "Employee Result: {}",
                state.getEmployeeResult()
        );

        logger.info(
                "RAG Result: {}",
                state.getRagResult()
        );

        logger.info(
                "======================================"
        );

        // -------------------------------------------------
        // Step 6: Agent analyzes aggregated workflow state
        // -------------------------------------------------

        logger.info(
                "Calling AgentWorkflowService for decision"
        );

        AgentDecision decision =
                agentWorkflowService.analyzeState(state);
        if (trace != null) trace.event("AGENT_DECISION", "analyzeState", "sufficientInformation=" + decision.sufficientInformation() + "; reasoning=" + decision.reasoning(), true);

        logger.info(
                "Agent decision: sufficientInformation={}",
                decision.sufficientInformation()
        );

        logger.info(
                "Agent reasoning: {}",
                decision.reasoning()
        );

        // -------------------------------------------------
        // Step 7: Handle insufficient information
        // -------------------------------------------------

        if (!decision.sufficientInformation()) {

            logger.warn(
                    "Agent determined that available information is insufficient"
            );

            String fallback =
                    "I do not have enough information to answer the request.";

            // -------------------------------------------------
            // OUTPUT GUARDRAIL
            // -------------------------------------------------

            logger.info(
                    "Running output guardrail for fallback response"
            );

            String validatedFallback =
                    guardrailService.validateOutput(
                            fallback
                    );

            logger.info(
                    "Fallback output guardrail PASSED"
            );

            state.setFinalAnswer(
                    validatedFallback
            );
            if (trace != null) trace.event("FINAL_ANSWER", "fallback", "Agent decided evidence was insufficient", true);

            return new WorkflowResponse(
                    "COMPLETED",
                    validatedFallback,
                    null,
                    null
            );
        }

        // -------------------------------------------------
        // Step 8: Agent generates final answer
        // -------------------------------------------------

        logger.info(
                "Agent determined that sufficient information is available"
        );

        String answer =
                agentWorkflowService.generateFinalAnswer(
                        state
                );

        logger.info(
                "AI final answer generated"
        );

        // -------------------------------------------------
        // OUTPUT GUARDRAIL
        // -------------------------------------------------

        logger.info(
                "Running output guardrail on final AI response"
        );

        String validatedAnswer =
                guardrailService.validateOutput(
                        answer
                );

        logger.info(
                "Final AI response output guardrail PASSED"
        );

        state.setFinalAnswer(
                validatedAnswer
        );
        if (trace != null) trace.event("FINAL_ANSWER", "generateFinalAnswer", "Final answer generated and passed output guardrail", true);

        // -------------------------------------------------
        // Step 9: Final response
        // -------------------------------------------------

        logger.info(
                "===== WORKFLOW ORCHESTRATOR END ====="
        );

        return new WorkflowResponse(
                "COMPLETED",
                validatedAnswer,
                null,
                null
        );
    }

    // -----------------------------------------------------
    // Resume workflow after human approval
    // -----------------------------------------------------

    public WorkflowResponse resumeAfterApproval(
            String approvalId) {

        logger.info(
                "========================================"
        );

        logger.info(
                "RESUME WORKFLOW AFTER APPROVAL"
        );

        logger.info(
                "Approval ID: {}",
                approvalId
        );

        logger.info(
                "========================================"
        );

        PendingApproval approval =
                approvalStore.get(approvalId);

        if (approval == null) {

            throw new IllegalArgumentException(
                    "Approval not found: " + approvalId
            );
        }

        logger.info(
                "Approval status: {}",
                approval.status()
        );

        // -------------------------------------------------
        // Approval must be APPROVED
        // -------------------------------------------------

        if (!"APPROVED".equals(approval.status())) {

            logger.warn(
                    "Workflow cannot resume. Approval status: {}",
                    approval.status()
            );

            return new WorkflowResponse(
                    "REJECTED",
                    null,
                    approvalId,
                    "Workflow cannot resume. Approval status: "
                            + approval.status()
            );
        }

        // -------------------------------------------------
        // TODO:
        // Execute approved MCP write operation here.
        // -------------------------------------------------

        logger.info(
                "Human approval confirmed"
        );

        logger.info(
                "Approved action: {}",
                approval.action()
        );

        return new WorkflowResponse(
                "COMPLETED",
                "Human approval received. Approved action can now be executed.",
                approvalId,
                null
        );
    }
}