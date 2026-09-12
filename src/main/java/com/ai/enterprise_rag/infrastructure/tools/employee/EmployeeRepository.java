package com.ai.enterprise_rag.infrastructure.tools.employee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository
        extends JpaRepository<Employee, String> {
}