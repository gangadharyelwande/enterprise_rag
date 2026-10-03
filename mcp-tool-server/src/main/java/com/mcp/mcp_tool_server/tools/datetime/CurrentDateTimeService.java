package com.mcp.mcp_tool_server.tools.datetime;


import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CurrentDateTimeService {

    // Formats date → September 7, 2026
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // Formats time → 6:42:15 PM EDT
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm:ss a z");

    public CurrentDateTimeResponse getCurrentDateTime(
            String location,
            String timeZone) {

        // Convert timezone string → ZoneId
        ZoneId zoneId = ZoneId.of(timeZone);

        // Get current date/time for that timezone
        ZonedDateTime now =
                ZonedDateTime.now(zoneId);

        // Build structured response
        return new CurrentDateTimeResponse(
                location,
                timeZone,
                now.format(DATE_FORMATTER),       // Date
                now.format(TIME_FORMATTER),        // Time
                now.getDayOfWeek().toString()      // Day
        );
    }
}