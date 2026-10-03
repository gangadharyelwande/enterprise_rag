package com.mcp.mcp_tool_server.service;

import org.springframework.stereotype.Component;

@Component
public class EmployeeInputValidator {

    public void validateEmployeeId(String employeeId) {

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException(
                    "employeeId must not be blank");
        }

        String normalized = employeeId.trim();

        if (!normalized.matches("E\\d{4}")) {
            throw new IllegalArgumentException(
                    "employeeId must have format E####, for example E1001");
        }
    }
}
