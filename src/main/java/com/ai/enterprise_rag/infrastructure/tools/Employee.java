package com.ai.enterprise_rag.infrastructure.tools;

public record Employee(
        String employeeId,
        String name,
        String department,
        int leaveBalance
) {}