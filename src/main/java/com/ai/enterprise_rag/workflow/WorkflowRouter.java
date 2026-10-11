package com.ai.enterprise_rag.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WorkflowRouter {

    private static final Logger logger =
            LoggerFactory.getLogger(WorkflowRouter.class);

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile("\\bE\\d{4}\\b", Pattern.CASE_INSENSITIVE);

    public WorkflowType route(String request) {

        /*
         * HITL / sensitive action must be checked FIRST.
         *
         * Example:
         * "Approve 5 days leave for employee E1001"
         *
         * This request contains both employee and leave keywords,
         * but it is primarily a WRITE/ACTION request.
         */
        logger.info("===== WORKFLOW ROUTER =====");
        logger.info("Incoming request: {}", request);
        logger.info(
                "HITL detected: {}",
                containsHumanApprovalAction(request)
        );

        if (containsHumanApprovalAction(request)) {
            return WorkflowType.HUMAN_APPROVAL;
        }

        boolean employeeRequest =
                EMPLOYEE_ID_PATTERN.matcher(request).find()
                        || containsEmployeeKeywords(request);

        boolean ragRequest =
                containsRagKeywords(request);

        if (employeeRequest && ragRequest) {
            return WorkflowType.EMPLOYEE_AND_RAG;
        }

        if (employeeRequest) {
            return WorkflowType.EMPLOYEE;
        }

        if (ragRequest) {
            return WorkflowType.RAG;
        }

        return WorkflowType.UNKNOWN;
    }

    public String extractEmployeeId(String request) {

        Matcher matcher = EMPLOYEE_ID_PATTERN.matcher(request);

        if (matcher.find()) {
            return matcher.group().toUpperCase();
        }

        return null;
    }

    private boolean containsHumanApprovalAction(String request) {

        String text = request.toLowerCase();

        boolean approvalAction =
                text.contains("approve")
                        || text.contains("approval")
                        || text.contains("approve");

        boolean leaveAction =
                text.contains("leave")
                        || text.contains("vacation")
                        || text.contains("time off");

        return approvalAction && leaveAction;
    }

    private boolean containsEmployeeKeywords(String request) {

        String text = request.toLowerCase();

        return text.contains("employee")
                || text.contains("manager")
                || text.contains("working hours")
                || text.contains("department")
                || text.contains("job title");
    }

    private boolean containsRagKeywords(String request) {

        String text = request.toLowerCase();

        return text.contains("policy")
                || text.contains("policies")
                || text.contains("vacation")
                || text.contains("leave")
                || text.contains("holiday")
                || text.contains("benefits")
                || text.contains("remote work")
                || text.contains("work from home");
    }
}
