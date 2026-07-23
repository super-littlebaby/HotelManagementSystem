package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.repository.EmployeeRepository;
import com.project.hotelmanagementsystem.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 员工Service实现类
 */
@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findById(Integer id) {
        return employeeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelId(Integer hotelId) {
        return employeeRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByRole(String role) {
        return employeeRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelIdAndRole(Integer hotelId, String role) {
        return employeeRepository.findByHotelIdAndRole(hotelId, role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Employee> findByHotelIdAndIsActive(Integer hotelId, Boolean isActive) {
        return employeeRepository.findByHotelIdAndIsActive(hotelId, isActive);
    }
}