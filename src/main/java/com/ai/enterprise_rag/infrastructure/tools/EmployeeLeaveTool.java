package com.ai.enterprise_rag.infrastructure.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLeaveTool {
    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeLeaveTool.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeLeaveTool(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Tool(description = "Get the remaining leave balance for an employee.")
    public ToolResult<Employee> getEmployeeLeaveBalance(
            @ToolParam(description = "Employee ID such as E101", required = true) String employeeId) {

        logger.info("Tool called: getEmployeeLeaveBalance, employeeId={}",employeeId);

        // -----------------------------------------
        // 1. INPUT / SCHEMA-LEVEL VALIDATION
        // -----------------------------------------

        if (employeeId == null || employeeId.isBlank()) {
            logger.warn("Invalid employeeId: missing or blank");

            return ToolResult.error("INVALID_ARGUMENT","employeeId is required.");
        }

        String normalizedEmployeeId = employeeId.trim().toUpperCase();

        // -----------------------------------------
        // 2. BUSINESS VALIDATION
        // -----------------------------------------

        var employee = employeeRepository.findById(normalizedEmployeeId);

        if (employee.isEmpty()) {
            logger.warn("Business validation failed: employee not found, employeeId={}",normalizedEmployeeId);

            return ToolResult.error("EMPLOYEE_NOT_FOUND","Employee " + normalizedEmployeeId + " was not found.");
        }

        // -----------------------------------------
        // 3. EXECUTION
        // -----------------------------------------

        Employee result = employee.get();

        logger.info("Tool execution successful: employeeId={}, leaveBalance={}",
                result.employeeId(),
                result.leaveBalance()
        );

        return ToolResult.success(result);
    }
}