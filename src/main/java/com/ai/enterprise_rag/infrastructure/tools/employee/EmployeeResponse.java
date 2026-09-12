package com.ai.enterprise_rag.infrastructure.tools.employee;

public record EmployeeResponse(
        String employeeId,
        String name,
        String department,
        int leaveBalance
) {
}