package com.mcp.mcp_tool_server.tools.resource;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.stereotype.Component;

@Component
public class HrPolicyResource {

    @McpResource(
            uri = "company://hr/leave-policy",
            name = "HR Leave Policy",
            description = "Company policy for employee leave"
    )
    public String getLeavePolicy() {

        return """
                HR Leave Policy

                Employees receive annual paid leave based on company policy.

                1. Leave requests should be submitted through the employee portal.
                2. Manager approval is required before leave is taken.
                3. Employees should provide advance notice whenever possible.
                4. Emergency leave should be communicated to the manager as soon as possible.
                5. Unused leave may be carried forward according to company policy.
                """;
    }
}
