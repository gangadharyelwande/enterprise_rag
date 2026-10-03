package com.ai.enterprise_rag.infrastructure.tools.datetime;

public record CurrentDateTimeResponse(
        String location,
        String timeZone,
        String date,
        String time,
        String dayOfWeek
) {
}