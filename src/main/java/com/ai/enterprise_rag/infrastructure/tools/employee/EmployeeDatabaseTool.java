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
    public ToolResult<LeaveBalanceResponse> getEmployeeLeaveBalance(
            @ToolParam(
                    description = "Employee ID such as E101",
                    required = true
            )
            String employeeId) {

        logger.info(
                "Tool called: getEmployeeLeaveBalance, employeeId={}",
                employeeId
        );

        // 1. Input validation
        if (employeeId == null || employeeId.isBlank()) {

            logger.warn(
                    "Invalid employeeId: missing or blank"
            );

            return ToolResult.error(
                    "INVALID_ARGUMENT",
                    "employeeId is required."
            );
        }

        String normalizedEmployeeId =
                employeeId.trim().toUpperCase();

        // 2. Database lookup
        logger.info("Before calling H2 DB -->");

        var employee =
                employeeRepository.findById(normalizedEmployeeId);

        logger.info("After calling H2 DB -->");

        if (employee.isEmpty()) {

            logger.warn(
                    "Employee not found, employeeId={}",
                    normalizedEmployeeId
            );

            return ToolResult.error(
                    "EMPLOYEE_NOT_FOUND",
                    "Employee "
                            + normalizedEmployeeId
                            + " was not found."
            );
        }

        // 3. Get employee data
        Employee employeeData = employee.get();

        // 4. Create leave-balance response
        LeaveBalanceResponse response =
                new LeaveBalanceResponse(
                        employeeData.getEmployeeId(),
                        employeeData.getLeaveBalance()
                );

        logger.info(
                "Leave balance found: employeeId={}, leaveBalance={}",
                response.employeeId(),
                response.leaveBalance()
        );

        // 5. Return structured result
        return ToolResult.success(response);
    }
}