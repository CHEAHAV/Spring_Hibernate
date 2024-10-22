package com.example.hibernate.service;


import com.example.hibernate.model.entities.Employee;
import com.example.hibernate.repository.EmployeeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

public interface EmployeeService {
    public void addEmployee(Employee employee);
    public List<Employee> getAllEmployees();
    public Employee getEmployeeById(int id);
    public void updateEmployee(Employee employee);
    public void deleteEmployee(int id);
}
