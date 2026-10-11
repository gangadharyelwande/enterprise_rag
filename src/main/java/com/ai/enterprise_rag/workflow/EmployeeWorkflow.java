package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.evaluation.agent.AgentExecutionTrace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmployeeWorkflow {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeWorkflow.class);

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile(
                    "\\bE\\d{4}\\b",
                    Pattern.CASE_INSENSITIVE
            );

    private final ToolCallbackProvider mcpTools;
    private final ObjectMapper objectMapper;

    public EmployeeWorkflow(
            ToolCallbackProvider mcpTools,
            ObjectMapper objectMapper) {

        this.mcpTools = mcpTools;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the employee branch independently.
     *
     * Important:
     * This method does NOT modify a shared WorkflowState.
     * It produces its own EmployeeResult.
     */
    public EmployeeResult execute(String request) {
        return execute(request, null);
    }

    public EmployeeResult execute(String request, AgentExecutionTrace trace) {

        long startTime = System.nanoTime();

        logger.info("===== EMPLOYEE WORKFLOW START =====");

        logger.info(
                "Employee workflow thread: {}",
                Thread.currentThread().getName()
        );

        try {

            logger.info(
                    "Employee request: {}",
                    request
            );

            // -------------------------------------------------
            // Step 1: Extract employee ID from the request
            // -------------------------------------------------

            Matcher matcher =
                    EMPLOYEE_ID_PATTERN.matcher(request);

            if (!matcher.find()) {

                throw new IllegalArgumentException(
                        "No valid employee ID found in request: "
                                + request
                );
            }

            String employeeId =
                    matcher.group().toUpperCase();

            logger.info(
                    "Extracted employeeId: {}",
                    employeeId
            );
            if (trace != null) trace.event("EMPLOYEE_ID_EXTRACTED", employeeId, "Employee identifier extracted from request", true);

            // -------------------------------------------------
            // Step 2: Find MCP tool
            // -------------------------------------------------

            ToolCallback employeeTool =
                    findTool("findEmployee");

            if (employeeTool == null) {

                throw new IllegalStateException(
                        "findEmployee MCP tool not available"
                );
            }

            // -------------------------------------------------
            // Step 3: Build MCP arguments
            // -------------------------------------------------

            String arguments =
                    objectMapper.writeValueAsString(
                            Map.of(
                                    "employeeId",
                                    employeeId
                            )
                    );

            logger.info(
                    "Calling MCP findEmployee with arguments: {}",
                    arguments
            );

            // -------------------------------------------------
            // Step 4: Optional deterministic evaluation fault
            // -------------------------------------------------
            // This is scoped to the AgentExecutionTrace created for one evaluation case.
            // It does not configure or mutate the shared MCP server failure simulator.
            if (trace != null && trace.shouldSimulateToolFailure("findEmployee")) {
                String message = "Simulated tool outage for evaluation: findEmployee";
                trace.toolCall("findEmployee", arguments, message, false, 0);
                trace.event("SIMULATED_TOOL_FAILURE", "findEmployee", message, false);
                logger.warn("Evaluation fault injection: failing findEmployee before MCP call");
                throw new IllegalStateException(message);
            }

            // -------------------------------------------------
            // Step 5: Call MCP
            // -------------------------------------------------

            long toolStart = System.nanoTime();
            String result;
            try {
                result = employeeTool.call(arguments);
                if (trace != null) trace.toolCall("findEmployee", arguments, result, true, (System.nanoTime() - toolStart) / 1_000_000);
            } catch (RuntimeException toolException) {
                if (trace != null) trace.toolCall("findEmployee", arguments, toolException.getMessage(), false, (System.nanoTime() - toolStart) / 1_000_000);
                throw toolException;
            }

            logger.info(
                    "MCP findEmployee result: {}",
                    result
            );

            // -------------------------------------------------
            // Step 5: Return independent result
            // -------------------------------------------------

            if (trace != null) trace.event("EMPLOYEE_WORKFLOW", "findEmployee", "Employee MCP branch completed", true);
            return new EmployeeResult(result);

        } catch (JsonProcessingException e) {
            if (trace != null) trace.event("EMPLOYEE_WORKFLOW", "serialization", e.getMessage(), false);
            throw new RuntimeException(e);
        } finally {

            long durationMillis =
                    (System.nanoTime() - startTime)
                            / 1_000_000;

            logger.info(
                    "EMPLOYEE workflow END - duration={} ms - thread={}",
                    durationMillis,
                    Thread.currentThread().getName()
            );

            logger.info(
                    "===== EMPLOYEE WORKFLOW END ====="
            );
        }
    }

    /**
     * Finds a specific MCP tool from the discovered tools.
     */
    private ToolCallback findTool(String name) {

        for (ToolCallback callback :
                mcpTools.getToolCallbacks()) {

            if (callback.getToolDefinition()
                    .name()
                    .equals(name)) {

                return callback;
            }
        }

        return null;
    }
}

