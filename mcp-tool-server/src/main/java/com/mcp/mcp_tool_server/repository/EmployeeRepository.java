package com.mcp.mcp_tool_server.repository;


import com.mcp.mcp_tool_server.model.Employee;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeRepository {

    private final List<Employee> employees = List.of(

            new Employee(
                    "E1001",
                    "John",
                    "Smith",
                    "Engineering",
                    "Senior Software Engineer",
                    "john.smith@company.com"
            ),

            new Employee(
                    "E1002",
                    "Sarah",
                    "Johnson",
                    "HR",
                    "Technical Recruiter",
                    "sarah.johnson@company.com"
            ),

            new Employee(
                    "E1003",
                    "Michael",
                    "Brown",
                    "Finance",
                    "Senior Accountant",
                    "michael.brown@company.com"
            )
    );

    public Optional<Employee> findByEmployeeId(String employeeId) {

        return employees.stream()
                .filter(employee ->
                        employee.employeeId().equalsIgnoreCase(employeeId))
                .findFirst();
    }
}
