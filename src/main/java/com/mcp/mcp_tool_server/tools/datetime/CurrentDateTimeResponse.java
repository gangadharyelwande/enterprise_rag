package com.mcp.mcp_tool_server.tools.datetime;

public record CurrentDateTimeResponse(
        String location,
        String timeZone,
        String date,
        String time,
        String dayOfWeek
) {
}