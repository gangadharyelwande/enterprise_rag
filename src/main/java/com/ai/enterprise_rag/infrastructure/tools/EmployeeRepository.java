package com.ai.enterprise_rag.infrastructure.tools;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository
        extends JpaRepository<Employee, String> {
}