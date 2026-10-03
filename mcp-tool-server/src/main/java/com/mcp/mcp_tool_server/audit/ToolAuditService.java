package com.mcp.mcp_tool_server.audit;

import org.springframework.stereotype.Service;

@Service
public class ToolAuditService {

    public void logToolInvocation(
            String toolName,
            String user,
            String input,
            String result) {

        System.out.println("[MCP-AUDIT] " +
                        "tool=" + toolName +
                        " user=" + user +
                        " input=" + input +
                        " result=" + result
        );
    }
}
