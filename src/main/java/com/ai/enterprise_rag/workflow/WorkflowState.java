package com.ai.enterprise_rag.workflow;

import com.ai.enterprise_rag.infrastructure.tools.employee.EmployeeResponse;

import java.util.ArrayList;
import java.util.List;

public class WorkflowState {

    private String originalRequest;

    private WorkflowType workflowType;

    private String employeeId;

    private EmployeeResponse employee;

    private String workingHours;

    private String finalAnswer;

    private final List<String> conversationHistory =
            new ArrayList<>();

    public WorkflowState(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public EmployeeResponse getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeResponse employee) {
        this.employee = employee;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getFinalAnswer() {
        return finalAnswer;
    }

    public void setFinalAnswer(String finalAnswer) {
        this.finalAnswer = finalAnswer;
    }

    public List<String> getConversationHistory() {
        return conversationHistory;
    }

    public void addHistory(String message) {
        conversationHistory.add(message);
    }
}