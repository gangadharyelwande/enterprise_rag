package com.mcp.mcp_tool_server.tools.datetime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateTimeMcpTool {

    private static final Logger logger =
            LoggerFactory.getLogger(CurrentDateTimeMcpTool.class);

    private final CurrentDateTimeService currentDateTimeService;

    public CurrentDateTimeMcpTool(
            CurrentDateTimeService currentDateTimeService) {

        this.currentDateTimeService =
                currentDateTimeService;
    }

    @McpTool(
            name = "getCurrentDateTime",
            description =
                    "Get the current date and time for a location."
    )
    public CurrentDateTimeResponse getCurrentDateTime(

            @McpToolParam(
                    description =
                            "City or location name, "
                                    + "for example New York, Mumbai, "
                                    + "London, or Tokyo",
                    required = true
            )
            String location,

            @McpToolParam(
                    description =
                            "IANA time zone ID, "
                                    + "for example America/New_York, "
                                    + "Asia/Kolkata, Europe/London, "
                                    + "or Asia/Tokyo",
                    required = true
            )
            String timeZone) {

        logger.info("========================================");
        logger.info("MCP TOOL CALLED");
        logger.info("Tool: getCurrentDateTime");
        logger.info("Location: {}", location);
        logger.info("Time Zone: {}", timeZone);
        logger.info("========================================");

        return currentDateTimeService.getCurrentDateTime(
                location,
                timeZone
        );
    }
}