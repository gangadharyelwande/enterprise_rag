package com.ai.enterprise_rag.infrastructure.tools.employee;

public record EmployeeResponse(
        String employeeId,
        String firstName,
        String lastName,
        String department,
        String jobTitle,
        String email
) {
}