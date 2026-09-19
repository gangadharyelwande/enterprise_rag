package com.mcp.mcp_tool_server.tools;


import com.mcp.mcp_tool_server.audit.ToolAuditService;
import com.mcp.mcp_tool_server.model.EmployeeResponse;
import com.mcp.mcp_tool_server.security.ToolAuthorizationService;
import com.mcp.mcp_tool_server.service.EmployeeInputValidator;
import com.mcp.mcp_tool_server.service.EmployeeService;
import com.mcp.mcp_tool_server.tools.datetime.CurrentDateTimeMcpTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeTool {
    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeTool.class);

    private static final String TOOL_NAME = "findEmployee";

    private final EmployeeService employeeService;
    private final EmployeeInputValidator inputValidator;
    private final ToolAuthorizationService toolAuthorizationService;
    private final ToolAuditService auditService;

    public EmployeeTool(
            EmployeeService employeeService,
            EmployeeInputValidator inputValidator,
            ToolAuthorizationService toolAuthorizationService,
            ToolAuditService auditService) {

        this.employeeService = employeeService;
        this.inputValidator = inputValidator;
        this.toolAuthorizationService = toolAuthorizationService;
        this.auditService = auditService;
    }

    @McpTool(
            name = TOOL_NAME,
            description = "Find an employee using their employee ID. "
                    + "Use this tool when you need basic employee information."
    )
    public EmployeeResponse findEmployee(
            @McpToolParam(
                    description = "Employee ID in the format E####, for example E1001",
                    required = true
            )
            String employeeId) {

        String user = "mcp-client";

        logger.info("========== MCP TOOL CALLED ==========");
        logger.info("Tool       : {}", TOOL_NAME);
        logger.info("Employee ID: {}", employeeId);
        logger.info("User       : {}", user);

        try {

            if (!toolAuthorizationService.isToolAllowed(TOOL_NAME)) {
                logger.warn("Tool denied: {}", TOOL_NAME);

                auditService.logToolInvocation(
                        TOOL_NAME,
                        user,
                        employeeId,
                        "DENIED - TOOL NOT ALLOWED"
                );

                throw new SecurityException(
                        "Tool is not allowed: " + TOOL_NAME);
            }

            inputValidator.validateEmployeeId(employeeId);

            String normalizedEmployeeId =
                    employeeId.trim().toUpperCase();

            logger.info("Normalized Employee ID: {}", normalizedEmployeeId);

            EmployeeResponse response =
                    employeeService.findEmployee(normalizedEmployeeId);

            logger.info("Employee found successfully: {}",
                    normalizedEmployeeId);

            auditService.logToolInvocation(
                    TOOL_NAME,
                    user,
                    normalizedEmployeeId,
                    "SUCCESS"
            );

            logger.info("========== MCP TOOL COMPLETED ==========");

            return response;

        } catch (RuntimeException runtimeException) {

            logger.error(
                    "MCP TOOL FAILED: {} - {}",
                    TOOL_NAME,
                    runtimeException.getMessage()
            );

            auditService.logToolInvocation(
                    TOOL_NAME,
                    user,
                    employeeId,
                    "FAILED - " + runtimeException.getMessage()
            );

            throw runtimeException;
        }
    }
}