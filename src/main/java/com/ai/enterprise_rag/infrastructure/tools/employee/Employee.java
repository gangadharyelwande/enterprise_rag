package com.ai.enterprise_rag.infrastructure.tools.employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    private String employeeId;

    private String name;

    private String department;

    private int leaveBalance;

    protected Employee() {
    }

    public Employee(
            String employeeId,
            String name,
            String department,
            int leaveBalance) {

        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.leaveBalance = leaveBalance;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getLeaveBalance() {
        return leaveBalance;
    }
}