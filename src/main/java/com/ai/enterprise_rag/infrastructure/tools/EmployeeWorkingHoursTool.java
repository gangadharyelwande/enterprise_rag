package com.ai.enterprise_rag.infrastructure.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class EmployeeWorkingHoursTool {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeWorkingHoursTool.class);

    @Tool(description = "get the standard employee working hours.")
    public String getEmployeeWorkingHours() {

        logger.info("Tool called: getEmployeeWorkingHours");

        String result = "Standard employee working hours are Monday-Friday, "
                        + "9:00 AM to 5:00 PM.";

        logger.info("Tool result: {}", result);

        return result;
    }

}
