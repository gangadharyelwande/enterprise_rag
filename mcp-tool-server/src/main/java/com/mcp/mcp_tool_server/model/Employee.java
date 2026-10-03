package com.mcp.mcp_tool_server.model;

public record Employee(
        String employeeId,
        String firstName,
        String lastName,
        String department,
        String jobTitle,
        String email
) { }
