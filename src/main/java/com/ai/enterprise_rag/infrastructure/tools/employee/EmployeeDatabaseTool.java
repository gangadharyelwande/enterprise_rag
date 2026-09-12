package com.ai.enterprise_rag.infrastructure.tools.employee;

import com.ai.enterprise_rag.infrastructure.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeDatabaseTool {
    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeDatabaseTool.class);

    private final EmployeeRepository employeeRepository;

    public EmployeeDatabaseTool(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Tool(description = "Get the remaining leave balance for an employee.")
    public ToolResult<EmployeeResponse> getEmployeeLeaveBalance(
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
        logger.info("Before calling H2 DB-->");
        var employee = employeeRepository.findById(normalizedEmployeeId);
        logger.info("After calling H2 DB-->");

        if (employee.isEmpty()) {
            logger.warn("Business validation failed: employee not found, employeeId={}",normalizedEmployeeId);

            return ToolResult.error("EMPLOYEE_NOT_FOUND","Employee " + normalizedEmployeeId + " was not found.");
        }

        // -----------------------------------------
        // 3. EXECUTION
        // -----------------------------------------

        Employee employeeData = employee.get();

        EmployeeResponse response =
                new EmployeeResponse(
                        employeeData.getEmployeeId(),
                        employeeData.getName(),
                        employeeData.getDepartment(),
                        employeeData.getLeaveBalance()
                );

        logger.info(
                "Employee found: {}, leaveBalance={}",
                response.employeeId(),
                response.leaveBalance()
        );

        // 4. Return structured result
        return ToolResult.success(response);
    }
}