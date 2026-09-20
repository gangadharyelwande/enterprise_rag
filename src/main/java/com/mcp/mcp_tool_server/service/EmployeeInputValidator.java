package com.mcp.mcp_tool_server.service;

import com.mcp.mcp_tool_server.exception.InvalidToolArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class EmployeeInputValidator {
    private static final Logger logger =
            LoggerFactory.getLogger(EmployeeInputValidator.class);

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile("^E\\d{4}$");

    public String validateAndNormalize(String employeeId) {

        if (employeeId == null || employeeId.isBlank()) {
//            logger.warn(
//                    "INVALID_ARGUMENT: Employee ID is missing"
//            ); //THIS SHOWS ON CONSOLE
            throw new InvalidToolArgumentException("Employee ID is required");
        }

        String normalizedEmployeeId = employeeId.trim().toUpperCase();

        if (!EMPLOYEE_ID_PATTERN.matcher(normalizedEmployeeId).matches()) {
//            logger.warn(
//                    "INVALID_ARGUMENT: employeeId={} does not match E####",
//                    normalizedEmployeeId
//            ); //THIS SHOWS ON CONSOLE
            throw new InvalidToolArgumentException("Employee ID must follow format E####");
        }

        return normalizedEmployeeId;
    }
}