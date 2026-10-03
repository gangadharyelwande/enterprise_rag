package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.infrastructure.tools.employee.EmployeeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmployeeWorkflow implements Workflow {

    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeWorkflow.class);

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile("\\bE\\d{4}\\b", Pattern.CASE_INSENSITIVE);

    private final ToolCallbackProvider mcpTools;
    private final ObjectMapper objectMapper;

    public EmployeeWorkflow(ToolCallbackProvider mcpTools) {

        this.mcpTools = mcpTools;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public WorkflowState execute(WorkflowState state) {

        logger.info("===== EMPLOYEE WORKFLOW START =====");

        String request = state.getOriginalRequest();

        logger.info("Employee workflow request: {}",request
        );

        // 1. Record workflow execution
        state.addHistory("Employee workflow started");

        // 2. Extract employee ID
        String employeeId = extractEmployeeId(request);

        logger.info("Employee ID extracted: {}",employeeId);

        state.setEmployeeId(employeeId);

        state.addHistory("Employee ID extracted: " + employeeId);

        // 3. Find employee using MCP
        logger.info("Looking for MCP tool: findEmployee");

        ToolCallback employeeTool = findTool("findEmployee");

        logger.info("MCP tool found: {}",employeeTool.getToolDefinition().name());
        logger.info("Calling MCP tool findEmployee for employee: {}",employeeId);

        try {
            String employeeResult = employeeTool.call(
                    "{\"employeeId\":\"" + employeeId + "\"}"
            );

            String employeeJson = McpToolResultParser.extractText(employeeResult);

            logger.info("Parsed employee result: {}",employeeJson);

            // Convert MCP result into EmployeeResponse
            EmployeeResponse employee;

            try {

                employee = objectMapper.readValue(
                        employeeJson,
                        EmployeeResponse.class
                );

            } catch (Exception exception) {
                logger.error("Failed to parse employee MCP result",exception);
                throw new IllegalStateException("Unable to parse employee information",exception);
            }

            // Store employee result in workflow state
            state.setEmployee(employee);
            state.addHistory("findEmployee completed for " + employeeId);

            logger.info(
                    "Employee state updated: {} {}",
                    employee.employeeId(),
                    employee.department()
            );

        }catch (Exception exception) {

                logger.error(
                        "findEmployee failed for employeeId={}",
                        employeeId,
                        exception
                );

                state.addHistory("findEmployee failed for " + employeeId);
                throw exception;
            }

        // 4. Get employee working hours using MCP
        logger.info("Looking for MCP tool: employeeWorkingHours");

        ToolCallback workingHoursTool = findTool("employeeWorkingHours");

        logger.info("MCP tool found: {}",workingHoursTool.getToolDefinition().name());
        logger.info("Calling MCP tool employeeWorkingHours for employee: {}",employeeId);

        String workingHoursResult =
                workingHoursTool.call(
                        "{\"employeeId\":\"" + employeeId + "\"}"
                );

        String workingHours =
                McpToolResultParser.extractText(
                        workingHoursResult
                );

        logger.info("Parsed working hours: {}",workingHours);

        // Store working hours in workflow state
        state.setWorkingHours(workingHours);
        state.addHistory("employeeWorkingHours completed for " + employeeId);

        logger.info("Working hours state updated");

        // 5. Workflow completed
        state.addHistory("Employee workflow completed");

        logger.info("Employee workflow completed for: {}",employeeId);
        logger.info("Workflow history: {}",state.getConversationHistory());

        logger.info("===== EMPLOYEE WORKFLOW END =====");

        return state;
    }

    private String extractEmployeeId(String request) {

        Matcher matcher =
                EMPLOYEE_ID_PATTERN.matcher(request);

        if (!matcher.find()) {

            logger.warn("No valid employee ID found in request");
            throw new IllegalArgumentException("Employee ID is required. Expected format E1234");
        }

        return matcher.group().toUpperCase();
    }

    private ToolCallback findTool(String toolName) {

        logger.info("Searching for MCP tool: {}",toolName);

        ToolCallback[] callbacks = mcpTools.getToolCallbacks();

        logger.info("Total MCP tools available: {}",callbacks.length);

        for (ToolCallback tool : callbacks) {

            String discoveredToolName = tool.getToolDefinition().name();

            logger.info("Available MCP tool: {}",discoveredToolName);

            if (discoveredToolName.equals(toolName)) {
                return tool;
            }
        }

        logger.error("Required MCP tool not found: {}",toolName);
        throw new IllegalStateException("MCP tool not found: " + toolName);
    }
}