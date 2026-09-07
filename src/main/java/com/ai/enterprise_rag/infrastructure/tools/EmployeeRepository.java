package com.ai.enterprise_rag.infrastructure.tools;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EmployeeRepository {

    private final Map<String, Employee> employees = Map.of(
            "E101", new Employee(
                    "E101",
                    "John",
                    "Engineering",
                    12
            ),
            "E102", new Employee(
                    "E102",
                    "Sarah",
                    "HR",
                    15
            ),
            "E103", new Employee(
                    "E103",
                    "Mike",
                    "Finance",
                    8
            )
    );

    public Optional<Employee> findById(String employeeId) {
        return Optional.ofNullable(employees.get(employeeId));
    }
}