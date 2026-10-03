package com.ai.enterprise_rag.infrastructure.tools.employee;

public record LeaveBalanceResponse(
        String employeeId,
        int leaveBalance
) {
}