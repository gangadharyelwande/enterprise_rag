package com.mcp.mcp_tool_server.security;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ToolAuthorizationService {

    private static final Set<String> ALLOWED_TOOLS = Set.of( "findEmployee");

    public boolean isToolAllowed(String toolName) {
        return ALLOWED_TOOLS.contains(toolName);
    }
}
