package com.mcp.mcp_tool_server.tools;

import com.mcp.mcp_tool_server.audit.ToolAuditService;
import com.mcp.mcp_tool_server.exception.ToolErrorCode;
import com.mcp.mcp_tool_server.exception.ToolExecutionException;
import com.mcp.mcp_tool_server.model.EmployeeResponse;
import com.mcp.mcp_tool_server.security.ToolAuthorizationService;
import com.mcp.mcp_tool_server.service.EmployeeInputValidator;
import com.mcp.mcp_tool_server.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
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
            description =
                    "Find an employee using their employee ID. "
                            + "Use this tool when you need basic employee information."
    )
    public EmployeeResponse findEmployee(
            @McpToolParam(
                    description ="Employee ID in the format E####, for example E1001",
                    required = true
            )
            String employeeId) {

        String user = "mcp-client";

        logger.info(
                "MCP tool called: tool={}, employeeId={}, user={}",
                TOOL_NAME,
                employeeId,
                user
        );

        // 1. Authorization
        if (!toolAuthorizationService.isToolAllowed(TOOL_NAME)) {

            logger.warn(
                    "MCP tool authorization denied: tool={}, user={}",
                    TOOL_NAME,
                    user
            );

            auditService.logToolInvocation(
                    TOOL_NAME,
                    user,
                    employeeId,
                    "DENIED"
            );

            throw new ToolExecutionException(
                    ToolErrorCode.UNAUTHORIZED,
                    "Tool is not authorized"
            );
        }

        // 2. Validation + normalization
        String normalizedEmployeeId =
                inputValidator.validateAndNormalize(employeeId);

        // 3. Business operation
        EmployeeResponse response =
                employeeService.findEmployee(normalizedEmployeeId);

        // 4. Success logging/audit
        logger.info(
                "MCP tool completed successfully: tool={}, employeeId={}, user={}",
                TOOL_NAME,
                normalizedEmployeeId,
                user
        );

        auditService.logToolInvocation(
                TOOL_NAME,
                user,
                normalizedEmployeeId,
                "SUCCESS"
        );

        return response;
    }
}