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
    private String employeeResult;
    private String ragResult;
    private final List<String> conversationHistory = new ArrayList<>();

    private boolean approvalRequired;
    private String approvalId;
    private String approvalStatus;
    private String pendingAction;

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

    public String getEmployeeResult() {
        return employeeResult;
    }

    public void setEmployeeResult(String employeeResult) {
        this.employeeResult = employeeResult;
    }

    public String getRagResult() {
        return ragResult;
    }

    public void setRagResult(String ragResult) {
        this.ragResult = ragResult;
    }

    public String getApprovalId() {
        return approvalId;
    }

    public void setApprovalId(String approvalId) {
        this.approvalId = approvalId;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getPendingAction() {
        return pendingAction;
    }

    public void setPendingAction(String pendingAction) {
        this.pendingAction = pendingAction;
    }
}