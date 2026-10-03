package com.ai.enterprise_rag.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class WorkflowRouter {

    private static final Logger logger =
            LoggerFactory.getLogger(WorkflowRouter.class);

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile("\\bE\\d{4}\\b", Pattern.CASE_INSENSITIVE);

    public WorkflowType route(String message) {

        logger.info("===== WORKFLOW ROUTING START =====");
        logger.info("Routing request: {}", message);

        if (message == null || message.isBlank()) {

            logger.warn("Request is empty. Route: UNKNOWN");

            return WorkflowType.UNKNOWN;
        }

        if (EMPLOYEE_ID_PATTERN.matcher(message).find()) {

            logger.info(
                    "Employee ID detected. Route: EMPLOYEE");

            return WorkflowType.EMPLOYEE;
        }

        String lowerCaseMessage =
                message.toLowerCase();

        if (lowerCaseMessage.contains("policy")
                || lowerCaseMessage.contains("vacation")
                || lowerCaseMessage.contains("leave")
                || lowerCaseMessage.contains("holiday")
                || lowerCaseMessage.contains("benefits")) {

            logger.info(
                    "RAG keyword detected. Route: RAG");

            return WorkflowType.RAG;
        }

        logger.warn(
                "No routing rule matched. Route: UNKNOWN");

        return WorkflowType.UNKNOWN;
    }
}