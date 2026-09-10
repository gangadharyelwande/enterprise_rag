package com.ai.enterprise_rag.infrastructure.tools;

public record CurrentDateTimeResponse(
        String location,
        String timeZone,
        String date,
        String time,
        String dayOfWeek
) {
}