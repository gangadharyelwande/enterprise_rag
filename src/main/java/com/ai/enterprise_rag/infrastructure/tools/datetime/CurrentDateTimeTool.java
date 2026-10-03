package com.ai.enterprise_rag.infrastructure.tools.datetime;

import com.ai.enterprise_rag.infrastructure.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateTimeTool {

    // Logger for tool execution/debugging
    private static final Logger log =
            LoggerFactory.getLogger(CurrentDateTimeTool.class);

    // Service contains the actual date/time logic
    private final CurrentDateTimeService currentDateTimeService;

    // Constructor injection
    public CurrentDateTimeTool(
            CurrentDateTimeService currentDateTimeService) {

        this.currentDateTimeService = currentDateTimeService;
    }

    // Exposes this method as an AI tool
    @Tool(
            description =
                    "Get the current date and time for a city or location. "
                            + "Use this tool whenever the user asks what time or date "
                            + "it currently is in a location."
    )
    public ToolResult<CurrentDateTimeResponse> getCurrentDateTime(

            // Tool input: city/location
            @ToolParam(
                    description =
                            "City or location name, for example New York, "
                                    + "London, Tokyo, or Mumbai",
                    required = true
            )
            String location,

            // Tool input: IANA timezone
            @ToolParam(
                    description =
                            "IANA time zone ID, for example "
                                    + "America/New_York, Europe/London, "
                                    + "Asia/Tokyo, or Asia/Kolkata",
                    required = true
            )
            String timeZone) {

        // Log tool invocation
        log.info(
                "Tool called: getCurrentDateTime, location={}, timeZone={}",
                location,
                timeZone
        );

        // Validate required location
        if (location == null || location.isBlank()) {

            return ToolResult.error(
                    "INVALID_ARGUMENT",
                    "Location is required."
            );
        }

        // Validate required timezone
        if (timeZone == null || timeZone.isBlank()) {

            return ToolResult.error(
                    "INVALID_ARGUMENT",
                    "Time zone is required."
            );
        }

        try {

            // Delegate date/time calculation to the Service
            CurrentDateTimeResponse result =
                    currentDateTimeService.getCurrentDateTime(
                            location.trim(),
                            timeZone.trim()
                    );

            // Log successful execution
            log.info(
                    "Time lookup successful: location={}, timeZone={}",
                    location,
                    timeZone
            );

            // Return successful structured result to LLM
            return ToolResult.success(result);

        } catch (Exception ex) {

            // Handle invalid timezone or service error
            log.warn(
                    "Invalid time zone: {}",
                    timeZone
            );

            // Return controlled error instead of throwing exception
            return ToolResult.error(
                    "INVALID_TIMEZONE",
                    "The supplied time zone is not valid."
            );
        }
    }
}