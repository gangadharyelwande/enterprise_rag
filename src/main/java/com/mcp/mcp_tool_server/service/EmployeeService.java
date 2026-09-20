package com.mcp.mcp_tool_server.service;


import com.mcp.mcp_tool_server.exception.EmployeeNotFoundException;
import com.mcp.mcp_tool_server.model.Employee;
import com.mcp.mcp_tool_server.model.EmployeeResponse;
import com.mcp.mcp_tool_server.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public EmployeeResponse findEmployee(String employeeId) {

        Employee employee = employeeRepository
                .findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return new EmployeeResponse(
                employee.employeeId(),
                employee.firstName(),
                employee.lastName(),
                employee.department(),
                employee.jobTitle(),
                employee.email()
        );
    }
}
