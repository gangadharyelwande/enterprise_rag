package com.ai.enterprise_rag.infrastructure.tools;

public record EmployeeResponse(
        String employeeId,
        String name,
        String department,
        int leaveBalance
) {
}